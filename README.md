# VocabBrain

An Android vocabulary trainer for Uzbek speakers learning English. Point it at a text —
typed, photographed, or a PDF/Word file — and it extracts the words worth learning, with
IPA, part of speech, a natural Uzbek translation and an example sentence. It then builds a
quiz from those words and schedules each one for spaced repetition.

## Features

- **Import from anywhere** — paste text, photograph a page (on-device OCR), or open a PDF,
  DOCX or TXT file.
- **AI analysis** — one call returns the vocabulary and a quiz tailored to the chosen
  question count, question types and time limit.
- **Two question types** — multiple choice over Uzbek meanings, and writing, where the
  English word is typed from an Uzbek prompt with a first-letter/length hint.
- **Per-question timer**; running out of time reveals the answer and scores it as missed.
- **Spaced repetition** — every answer reschedules its word (SM-2 without the six-point
  grade: 1 day, 3 days, then interval x ease; a wrong answer resets the streak).
- **Cross-device sync** — an anonymous account per install, vocabulary merged through
  Firestore, with Room as the offline source of truth.

## Tech stack

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3, type-safe Navigation routes |
| Architecture | MVI (state / intent / effect), feature-first packages |
| DI | Koin |
| Persistence | Room |
| AI | Firebase AI Logic (Gemini), guarded by App Check |
| OCR | ML Kit text recognition (on device) |
| Documents | PdfBox-Android for PDF; DOCX read directly from its zip |
| Sync | Firebase Auth (anonymous) + Firestore |
| Serialization | kotlinx.serialization |
| Tests | JUnit4, Turbine, Truth, coroutines-test, Koin module verification |

## Architecture

The MVI loop lives in `core/mvi`:

```
Screen --onIntent--> handleIntent --setState--> state --render--> Screen
                                  \--sendEffect--> effect (one-shot)
```

- **State** is the only thing rendered, changed only through a pure `setState { copy(...) }`.
- **Effects** are consumed once — navigation, snackbars — and never replayed on rotation.
- Each screen declares its whole contract in one `XContract.kt` file: state, intents, effects.
- Every screen has a stateful `XRoute` (binds the ViewModel) and a stateless `XScreen`
  (renders state, emits intents), plus an `XStateProvider` listing every state it can be in
  so previews and screenshot tests cover all of them.

Asynchronous reads use `core/architecture/AsyncData` — `Uninitialized / Loading / Success /
Failure` — instead of an `isLoading` + `data` + `error` trio that can express impossible
combinations. `Loading` and `Failure` carry the previous data, so a refresh never blanks
the screen.

Features are vertical slices; each owns its `data`, `domain` and `presentation` layers, and
depends on other features only through their domain interfaces.

```
uz/sharif/vocabbrain/
├── core/{mvi, architecture, database, time, ui/{theme, icon, preview}}
├── di/AppModules.kt              Koin modules: database, firebase, engines, sync, repos, use cases, view models
├── navigation/{Screen, VocabNavHost}
└── feature/
    ├── word/          vocabulary list and detail
    ├── importvocab/   OCR, document parsing, prompt, analysis, import
    ├── quiz/          question generation and play
    ├── review/        spaced-repetition scheduling
    ├── result/        quiz result screen
    └── sync/          anonymous auth and Firestore merge
```

## The analysis contract

`feature/importvocab/data/remote` holds both halves of the model contract:

- `VOCAB_BRAIN_SYSTEM_PROMPT` — role, processing rules and the exact JSON schema. It never
  changes between requests, so it goes in the system turn.
- `buildVocabBrainPrompt(rawText, questionCount, allowedTypes, timePerQuestion)` — the
  per-request turn.

The reply is parsed by `VocabBrainResponseParser` into `VocabBrainDto`, which mirrors the
schema field for field (`extracted_vocabulary`, `quiz_settings`, `questions`). Tests pin the
schema, so a drift in field names fails the build instead of the app.

## Setup

Requirements: Android Studio (AGP 9.x), JDK 17, a Firebase project.

1. Register an Android app with the package `uz.sharif.vocabbrain` in the Firebase console
   and drop the generated `google-services.json` into `app/`.
2. In the console, enable:
   - **AI Logic** (Gemini),
   - **Authentication → Anonymous**,
   - **Firestore** (create the database),
   - **App Check**.
3. Deploy the Firestore rules in `firestore.rules` — they restrict every document to its
   owner:

   ```
   firebase deploy --only firestore:rules
   ```

4. Run a debug build once and register the App Check debug token it prints to Logcat.
   Without it, AI calls from debug builds are rejected.
5. Add the release certificate's SHA-1 and SHA-256 to the Firebase app settings.

No API key is stored in the app: the model is reached through Firebase AI Logic, and App
Check is what keeps other callers off the project's quota.

## Building

```bash
./gradlew assembleDebug        # debug APK
./gradlew testDebugUnitTest    # unit tests
./gradlew assembleRelease      # signed release APK (needs keystore.properties)
./gradlew bundleRelease        # AAB for Play Store
```

### Release signing

Signing details are read from `keystore.properties` in the project root, which is not in
version control. Without the file a release build still assembles, only unsigned.

```properties
storeFile=/absolute/path/to/vocabbrain-release.p12
storePassword=...
keyAlias=vocabbrain
keyPassword=...
```

Create the keystore with:

```bash
keytool -genkeypair -v -keystore ~/vocabbrain-release.p12 -storetype PKCS12 \
  -alias vocabbrain -keyalg RSA -keysize 4096 -validity 10000
```

Keep the file and its password backed up. Play Store updates are only possible with the
same key.

Release builds run R8 with resource shrinking. `app/proguard-rules.pro` keeps what is
resolved by name at runtime: kotlinx.serialization serializers, the navigation `Screen`
routes, PdfBox and the Room database.

## Testing

`./gradlew testDebugUnitTest` runs the whole suite (48 tests). What it covers:

- **ViewModel loops** — list, detail, import and quiz, driven through their intents and
  asserted on state and effects (Turbine + Truth).
- **The wire contract** — the analysis JSON parses into the DTOs, both question types
  survive the mapping, and an unknown question type is rejected with the offending value.
- **Prompt building** — defaults, config parameters, empty input, and the fact that the
  schema stays out of the per-request turn.
- **Review scheduling** — interval growth, reset on a wrong answer, ease-factor bounds.
- **Sync merge** — newer side wins, one-sided words are copied, equal timestamps move nothing.
- **DOCX extraction** — paragraphs, runs, breaks, tabs, table cells, and a malformed file.
- **The DI graph** — every Koin definition can be built, so a missing binding fails the
  build rather than the first screen that needs it.

## Known gaps

- The quiz generator used when no import is pending is an offline stub built from stored
  vocabulary; the AI path only runs through import.
- Sync runs at app start. Words imported during a session upload on the next launch.
- Deletions are not synced — nothing in the app deletes a word yet, and a missing document
  would be indistinguishable from one that never synced.
- The database uses destructive fallback for schema changes. Safe while unreleased; a
  release version needs real migrations.
- The release APK is ~51 MB because it carries every ABI plus the bundled ML Kit model.
  The AAB is ~33 MB and Play delivers far less per device; an ABI split or the unbundled
  ML Kit artifact would shrink a directly distributed APK.
