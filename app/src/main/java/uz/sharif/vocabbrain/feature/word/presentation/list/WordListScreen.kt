package uz.sharif.vocabbrain.feature.word.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.Intent
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.State
import kotlinx.coroutines.flow.Flow

/** Stateful entry point: binds the ViewModel loop to the stateless content. */
@Composable
fun WordListRoute(
    viewModel: WordListViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToQuiz: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    EffectHandler(viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToDetail -> onNavigateToDetail(effect.wordId)
            Effect.NavigateToImport -> onNavigateToImport()
            Effect.NavigateToQuiz -> onNavigateToQuiz()
            is Effect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
        }
    }

    WordListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun EffectHandler(effects: Flow<Effect>, onEffect: suspend (Effect) -> Unit) {
    LaunchedEffect(effects) { effects.collect { onEffect(it) } }
}

/** Stateless content: renders state, emits intents. Preview- and test-friendly. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    state: State,
    onIntent: (Intent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VocabBrain") },
                actions = {
                    IconButton(onClick = { onIntent(Intent.ImportClicked) }) {
                        Icon(painter = VocabIcons.edit(), contentDescription = "Import words")
                    }
                    IconButton(onClick = { onIntent(Intent.QuizClicked) }) {
                        Icon(painter = VocabIcons.goals(), contentDescription = "Start a quiz")
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(Intent.QueryChanged(it)) },
                label = { Text("Qidirish") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.words is AsyncData.Failure) {
                Text(
                    text = state.words.error.message ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            when {
                state.words.isLoading && state.visibleWords.isEmpty() -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                state.visibleWords.isEmpty() -> Text(
                    text = if (state.query.isBlank()) "Hozircha so'z yo'q" else "\"${state.query}\" bo'yicha topilmadi",
                    style = MaterialTheme.typography.bodyMedium,
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.visibleWords, key = { it.id }) { word ->
                        WordRow(
                            word = word,
                            onClick = { onIntent(Intent.WordClicked(word.id)) },
                            onLearnedToggle = { onIntent(Intent.LearnedToggled(word)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRow(word: Word, onClick: () -> Unit, onLearnedToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${word.term} ${word.phonetic}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = word.translationUz, style = MaterialTheme.typography.bodySmall)
        }
        Checkbox(checked = word.isLearned, onCheckedChange = { onLearnedToggle() })
        Icon(
            painter = VocabIcons.chevronRight(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun WordListScreenPreview(
    @PreviewParameter(WordListStateProvider::class) state: State,
) = VocabPreview {
    WordListScreen(state = state, onIntent = {})
}
