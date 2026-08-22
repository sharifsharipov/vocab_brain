package uz.sharif.vocabbrain.feature.result.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.sharif.vocabbrain.core.ui.icon.VocabIcons
import uz.sharif.vocabbrain.core.ui.preview.PreviewsDayNight
import uz.sharif.vocabbrain.core.ui.preview.VocabPreview
import uz.sharif.vocabbrain.core.ui.theme.VocabTheme
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.Effect
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.Intent
import uz.sharif.vocabbrain.feature.result.presentation.ResultContract.State
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewedWord

@Composable
fun ResultRoute(
    viewModel: ResultViewModel,
    onNavigateToQuiz: () -> Unit,
    onNavigateToWords: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.NavigateToQuiz -> onNavigateToQuiz()
                Effect.NavigateToWords -> onNavigateToWords()
            }
        }
    }

    ResultScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(state: State, onIntent: (Intent) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Natija") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val result = state.result
            if (result == null) {
                Text("Hozircha natija yo'q", style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(
                    painter = VocabIcons.achievements(),
                    contentDescription = null,
                    tint = VocabTheme.colors.success,
                )
                Text(
                    text = "${result.correct} / ${result.total}",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = "${result.accuracyPercent}% to'g'ri · ${result.wrong} ta xato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VocabTheme.colors.textSecondary,
                )
                Text(
                    text = "Takrorlash rejasi",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(result.reviewedWords, key = { it.term }) { word -> ReviewedWordRow(word) }
                }
            }

            Button(
                onClick = { onIntent(Intent.PlayAgainClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Yana bir test")
            }
            OutlinedButton(
                onClick = { onIntent(Intent.DoneClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("So'zlarga qaytish")
            }
        }
    }
}

@Composable
private fun ReviewedWordRow(word: ReviewedWord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = word.term,
            style = MaterialTheme.typography.bodyLarge,
            color = if (word.wasCorrect) VocabTheme.colors.success else MaterialTheme.colorScheme.error,
        )
        Text(
            text = "${word.intervalDays} kundan keyin",
            style = MaterialTheme.typography.bodySmall,
            color = VocabTheme.colors.textSecondary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ResultScreenPreview(
    @PreviewParameter(ResultStateProvider::class) state: State,
) = VocabPreview {
    ResultScreen(state = state, onIntent = {})
}
