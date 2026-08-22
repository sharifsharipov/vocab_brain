package uz.sharif.vocabbrain.feature.importvocab.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.ui.icon.VocabIcons
import uz.sharif.vocabbrain.core.ui.preview.PreviewsDayNight
import uz.sharif.vocabbrain.core.ui.preview.VocabPreview
import uz.sharif.vocabbrain.core.ui.theme.VocabTheme
import uz.sharif.vocabbrain.feature.importvocab.data.parser.DocumentParserImpl
import uz.sharif.vocabbrain.feature.importvocab.domain.model.ExtractedWord
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Effect
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.Intent
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportContract.State
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizConfig

@Composable
fun ImportRoute(
    viewModel: ImportViewModel,
    onNavigateToQuiz: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                Effect.NavigateToQuiz -> onNavigateToQuiz()
                Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    ImportScreen(state = state, onIntent = viewModel::onIntent, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    state: State,
    onIntent: (Intent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matndan so'z import qilish") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(painter = VocabIcons.arrowBack(), contentDescription = "Orqaga")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.text,
                onValueChange = { onIntent(Intent.TextChanged(it)) },
                label = { Text("Matnni joylashtiring") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            QuizConfigSection(config = state.config, onIntent = onIntent)

            val imagePicker = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri -> uri?.let { onIntent(Intent.ImageSelected(it)) } }

            val documentPicker = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri -> uri?.let { onIntent(Intent.DocumentSelected(it)) } }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !state.isReadingSource,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Rasm (OCR)")
                }
                OutlinedButton(
                    onClick = { documentPicker.launch(DocumentParserImpl.SUPPORTED_MIME_TYPES) },
                    enabled = !state.isReadingSource,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("PDF / Word")
                }
            }

            if (state.isReadingSource) {
                Text(
                    text = "Matn o'qilmoqda…",
                    style = MaterialTheme.typography.bodySmall,
                    color = VocabTheme.colors.textSecondary,
                )
            }

            OutlinedButton(
                onClick = { onIntent(Intent.AnalyzeClicked) },
                enabled = state.canAnalyze,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.analysis.isLoading) "Tahlil qilinmoqda…" else "Tahlil qilish")
            }

            if (state.analysis is AsyncData.Failure) {
                Text(
                    text = state.analysis.error.message ?: "Tahlil qilib bo'lmadi",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            when {
                state.analysis.isLoading && state.extractedWords.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

                state.extractedWords.isEmpty() -> Text(
                    text = "Topilgan so'zlar shu yerda ko'rinadi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocabTheme.colors.textSecondary,
                )

                else -> {
                    Text(
                        text = "${state.extractedWords.size} ta so'z, ${state.questionCount} ta savol tayyor",
                        style = MaterialTheme.typography.bodySmall,
                        color = VocabTheme.colors.textSecondary,
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(state.extractedWords, key = { it.term }) { word ->
                            ExtractedWordRow(
                                word = word,
                                isSelected = word.term in state.selectedTerms,
                                onToggle = { onIntent(Intent.WordToggled(word.term)) },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onIntent(Intent.ImportClicked) },
                enabled = state.canImport,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Text("${state.selectedWords.size} ta so'zni saqlash va testni boshlash")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizConfigSection(config: QuizConfig, onIntent: (Intent) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        ConfigRow(label = "Savollar soni") {
            QuizConfig.QUESTION_COUNT_CHOICES.forEach { count ->
                FilterChip(
                    selected = config.questionCount == count,
                    onClick = { onIntent(Intent.QuestionCountChanged(count)) },
                    label = { Text("$count") },
                )
            }
        }
        ConfigRow(label = "Savol turi") {
            QuestionType.entries.forEach { type ->
                FilterChip(
                    selected = type in config.questionTypes,
                    onClick = { onIntent(Intent.QuestionTypeToggled(type)) },
                    label = { Text(if (type == QuestionType.MULTIPLE_CHOICE) "Variantli" else "Yozma") },
                )
            }
        }
        ConfigRow(label = "Har savolga vaqt") {
            QuizConfig.TIME_CHOICES.forEach { seconds ->
                FilterChip(
                    selected = config.timePerQuestionSeconds == seconds,
                    onClick = { onIntent(Intent.TimePerQuestionChanged(seconds)) },
                    label = { Text("${seconds}s") },
                )
            }
        }
    }
}

@Composable
private fun ConfigRow(label: String, chips: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = VocabTheme.colors.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}

@Composable
private fun ExtractedWordRow(word: ExtractedWord, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isSelected, onValueChange = { onToggle() })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isSelected, onCheckedChange = null)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "${word.term} ${word.phonetic}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${word.partOfSpeech} · ${word.translationUz}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = word.exampleSentence,
                style = MaterialTheme.typography.bodySmall,
                color = VocabTheme.colors.textSecondary,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ImportScreenPreview(
    @PreviewParameter(ImportStateProvider::class) state: State,
) = VocabPreview {
    ImportScreen(state = state, onIntent = {})
}
