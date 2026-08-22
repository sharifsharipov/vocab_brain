package uz.sharif.vocabbrain.di

import androidx.room.Room
import uz.sharif.vocabbrain.core.database.AppDatabase
import uz.sharif.vocabbrain.core.time.TimeProvider
import uz.sharif.vocabbrain.feature.importvocab.data.ocr.MlKitOcrManager
import uz.sharif.vocabbrain.feature.importvocab.data.ocr.OcrManager
import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocumentParser
import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocumentParserImpl
import uz.sharif.vocabbrain.feature.importvocab.data.remote.FirebaseVocabEngine
import uz.sharif.vocabbrain.feature.importvocab.data.remote.ImportRemoteDataSource
import uz.sharif.vocabbrain.feature.importvocab.data.repository.VocabImportRepositoryImpl
import uz.sharif.vocabbrain.feature.importvocab.domain.repository.VocabImportRepository
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.AnalyzeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ExtractDocumentTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.ImportWordsUseCase
import uz.sharif.vocabbrain.feature.importvocab.domain.usecase.RecognizeTextUseCase
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportViewModel
import uz.sharif.vocabbrain.feature.quiz.data.remote.QuizRemoteDataSource
import uz.sharif.vocabbrain.feature.quiz.data.remote.StubQuizRemoteDataSource
import uz.sharif.vocabbrain.feature.quiz.data.repository.QuizRepositoryImpl
import uz.sharif.vocabbrain.feature.quiz.domain.repository.QuizRepository
import uz.sharif.vocabbrain.feature.quiz.domain.usecase.StartQuizUseCase
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizViewModel
import uz.sharif.vocabbrain.feature.result.presentation.ResultViewModel
import uz.sharif.vocabbrain.feature.review.data.repository.ReviewRepositoryImpl
import uz.sharif.vocabbrain.feature.review.domain.repository.ReviewRepository
import uz.sharif.vocabbrain.feature.review.domain.usecase.GetLastResultUseCase
import uz.sharif.vocabbrain.feature.review.domain.usecase.SubmitQuizResultUseCase
import uz.sharif.vocabbrain.feature.word.data.repository.WordRepositoryImpl
import uz.sharif.vocabbrain.feature.word.domain.repository.WordRepository
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ObserveWordsUseCase
import uz.sharif.vocabbrain.feature.word.domain.usecase.ToggleWordLearnedUseCase
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailViewModel
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListViewModel
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import uz.sharif.vocabbrain.feature.importvocab.data.remote.VOCAB_BRAIN_SYSTEM_PROMPT
import uz.sharif.vocabbrain.feature.sync.data.AuthManager
import uz.sharif.vocabbrain.feature.sync.data.FirestoreWordDataSource
import uz.sharif.vocabbrain.feature.sync.domain.SyncWordsUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "vocab_brain.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<AppDatabase>().wordDao() }
    single { TimeProvider.System }
}

// 2. Network and engines (OCR + Gemini)
val networkAndEnginesModule = module {
    single<OcrManager> { MlKitOcrManager(androidContext()) }
    single<DocumentParser> { DocumentParserImpl(androidContext()) }

    single<ImportRemoteDataSource> { FirebaseVocabEngine(model = get()) }

    single<QuizRemoteDataSource> { StubQuizRemoteDataSource() }
}

/**
 * Objects the Firebase SDK builds for us. Kept apart because they are created by the
 * framework, not by a constructor Koin can inspect, so [appModules] verification skips them.
 */
val firebaseModule = module {
    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }

    // The model runs behind Firebase AI Logic: no API key is shipped inside the APK.
    single<GenerativeModel> {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content { text(VOCAB_BRAIN_SYSTEM_PROMPT) },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            },
        )
    }
}

// 3. Account and cross-device sync
val syncModule = module {
    single { AuthManager(auth = get()) }
    single { FirestoreWordDataSource(firestore = get()) }
    factory { SyncWordsUseCase(authManager = get(), remoteDataSource = get(), wordDao = get()) }
}

// 4. Repositories
val repositoryModule = module {
    single<WordRepository> { WordRepositoryImpl(wordDao = get(), timeProvider = get()) }
    single<QuizRepository> { QuizRepositoryImpl(remoteDataSource = get(), wordRepository = get()) }
    single<ReviewRepository> { ReviewRepositoryImpl(wordDao = get(), timeProvider = get()) }
    single<VocabImportRepository> {
        VocabImportRepositoryImpl(
            remoteDataSource = get(),
            wordRepository = get(),
            quizRepository = get(),
        )
    }
}

// 5. Use cases
val useCaseModule = module {
    factory { ObserveWordsUseCase(get()) }
    factory { ObserveWordUseCase(get()) }
    factory { ToggleWordLearnedUseCase(get()) }
    factory { AnalyzeTextUseCase(get()) }
    factory { ImportWordsUseCase(get()) }
    factory { RecognizeTextUseCase(get()) }
    factory { ExtractDocumentTextUseCase(get()) }
    factory { StartQuizUseCase(get()) }
    factory { SubmitQuizResultUseCase(get()) }
    factory { GetLastResultUseCase(get()) }
}

// 6. ViewModels
val viewModelModule = module {
    viewModel { WordListViewModel(observeWords = get(), toggleWordLearned = get()) }

    // The detail screen passes its word id in: koinViewModel { parametersOf(wordId) }
    viewModel { (wordId: String) ->
        WordDetailViewModel(wordId = wordId, observeWord = get(), toggleWordLearned = get())
    }

    viewModel {
        ImportViewModel(
            analyzeText = get(),
            importWords = get(),
            recognizeText = get(),
            extractDocumentText = get(),
        )
    }

    viewModel { QuizViewModel(startQuiz = get(), submitQuizResult = get()) }

    viewModel { ResultViewModel(getLastResult = get()) }
}

val appModules = listOf(
    databaseModule,
    firebaseModule,
    networkAndEnginesModule,
    syncModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
