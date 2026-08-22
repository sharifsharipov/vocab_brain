package uz.sharif.vocabbrain.feature.word.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.ui.icon.VocabIcons
import uz.sharif.vocabbrain.core.ui.theme.VocabTheme
import uz.sharif.vocabbrain.core.ui.preview.PreviewsDayNight
import uz.sharif.vocabbrain.core.ui.preview.VocabPreview
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Effect
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.Intent
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.State

@Composable
fun WordDetailRoute(
    viewModel: WordDetailViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    WordDetailScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(state: State, onIntent: (Intent) -> Unit) {
    val word = state.word.dataOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word?.term.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(painter = VocabIcons.arrowBack(), contentDescription = "Orqaga")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                word != null -> {
                    Text(
                        text = "${word.phonetic} · ${word.partOfSpeech}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VocabTheme.colors.textSecondary,
                    )
                    Text(text = word.translationUz, style = MaterialTheme.typography.headlineLarge)
                    Text(text = word.exampleSentence, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = word.exampleTranslationUz,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VocabTheme.colors.textSecondary,
                    )
                    Button(onClick = { onIntent(Intent.LearnedToggled) }) {
                        Text(if (word.isLearned) "O'rganilmagan deb belgilash" else "O'rgandim")
                    }
                }

                state.word is AsyncData.Failure -> Text(
                    text = state.word.error.message ?: "So'z topilmadi",
                    color = MaterialTheme.colorScheme.error,
                )

                else -> CircularProgressIndicator()
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun WordDetailScreenPreview(
    @PreviewParameter(WordDetailStateProvider::class) state: State,
) = VocabPreview {
    WordDetailScreen(state = state, onIntent = {})
}
