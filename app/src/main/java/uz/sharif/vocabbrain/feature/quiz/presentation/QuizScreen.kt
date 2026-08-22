package uz.sharif.vocabbrain.feature.quiz.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.core.ui.icon.VocabIcons
import uz.sharif.vocabbrain.core.ui.preview.PreviewsDayNight
import uz.sharif.vocabbrain.core.ui.preview.VocabPreview
import uz.sharif.vocabbrain.core.ui.theme.VocabTheme
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuestionType
import uz.sharif.vocabbrain.feature.quiz.domain.model.QuizQuestion
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Effect
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.Intent
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizContract.State

@Composable
fun QuizRoute(
    viewModel: QuizViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.NavigateToResult -> onNavigateToResult()
                Effect.NavigateBack -> onNavigateBack()
                is Effect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    QuizScreen(state = state, onIntent = viewModel::onIntent, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: State,
    onIntent: (Intent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.CloseClicked) }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val question = state.currentQuestion
            when {
                state.quiz is AsyncData.Failure -> Text(
                    text = state.quiz.error.message ?: "Test tuzib bo'lmadi",
                    color = MaterialTheme.colorScheme.error,
                )

                question == null -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                else -> QuestionContent(state = state, question = question, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun QuestionContent(state: State, question: QuizQuestion, onIntent: (Intent) -> Unit) {
    LinearProgressIndicator(
        progress = { state.questionNumber.toFloat() / state.questions.size },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${state.questionNumber} / ${state.questions.size}",
            style = MaterialTheme.typography.bodySmall,
            color = VocabTheme.colors.textSecondary,
        )
        Text(
            text = "${state.secondsLeft}s",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.secondsLeft <= LOW_TIME_SECONDS) VocabTheme.colors.warning
            else VocabTheme.colors.textSecondary,
        )
    }
    Text(text = question.prompt, style = MaterialTheme.typography.titleLarge)

    when (question.type) {
        QuestionType.MULTIPLE_CHOICE -> question.options.forEach { option ->
            OptionCard(
                option = option,
                isSelected = state.answerForCurrent == option,
                isRevealed = state.isCurrentAnswered,
                isCorrect = question.isCorrect(option),
                onClick = { onIntent(Intent.OptionSelected(option)) },
            )
        }

        QuestionType.WRITING -> WritingAnswer(state = state, question = question, onIntent = onIntent)
    }

    if (state.isCurrentAnswered) {
        AnswerFeedback(state = state, question = question)
    }

    Button(
        onClick = { onIntent(Intent.NextClicked) },
        enabled = state.isCurrentAnswered,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (state.isLastQuestion) "Natijani ko'rish" else "Keyingi")
    }
}

@Composable
private fun WritingAnswer(state: State, question: QuizQuestion, onIntent: (Intent) -> Unit) {
    OutlinedTextField(
        value = if (state.isCurrentAnswered) state.answerForCurrent.orEmpty() else state.writingDraft,
        onValueChange = { onIntent(Intent.WritingChanged(it)) },
        label = { Text("Javobingiz") },
        singleLine = true,
        enabled = !state.isCurrentAnswered,
        supportingText = question.hint?.let { hint -> { Text(hint) } },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedButton(
        onClick = { onIntent(Intent.WritingSubmitted) },
        enabled = !state.isCurrentAnswered && state.writingDraft.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Tekshirish")
    }
}

/** After an answer, the right option turns green and a wrong pick turns red. */
@Composable
private fun OptionCard(
    option: String,
    isSelected: Boolean,
    isRevealed: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit,
) {
    val accent: Color? = when {
        !isRevealed -> null
        isCorrect -> VocabTheme.colors.success
        isSelected -> MaterialTheme.colorScheme.error
        else -> null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, enabled = !isRevealed, onClick = onClick),
        border = accent?.let { BorderStroke(width = 2.dp, color = it) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Text(
            text = option,
            style = MaterialTheme.typography.bodyLarge,
            color = accent ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun AnswerFeedback(state: State, question: QuizQuestion) {
    val isCorrect = state.answerForCurrent?.let(question::isCorrect) == true
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = when {
                isCorrect -> "To'g'ri"
                state.isCurrentTimedOut -> "Vaqt tugadi — to'g'ri javob: ${question.correctAnswer}"
                else -> "Noto'g'ri — to'g'ri javob: ${question.correctAnswer}"
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (isCorrect) VocabTheme.colors.success else MaterialTheme.colorScheme.error,
        )
        Text(
            text = question.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = VocabTheme.colors.textSecondary,
        )
    }
}

private const val LOW_TIME_SECONDS = 5

@PreviewsDayNight
@Composable
internal fun QuizScreenPreview(
    @PreviewParameter(QuizStateProvider::class) state: State,
) = VocabPreview {
    QuizScreen(state = state, onIntent = {})
}
