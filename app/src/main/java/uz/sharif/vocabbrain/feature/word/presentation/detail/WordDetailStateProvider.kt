package uz.sharif.vocabbrain.feature.word.presentation.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.model.aWord
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailContract.State

class WordDetailStateProvider : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            aWordDetailState(),
            aWordDetailState(word = AsyncData.Success(aWord(isLearned = true))),
            aWordDetailState(word = AsyncData.Loading()),
            aWordDetailState(word = AsyncData.Failure(WordNotFoundException("1"))),
        )
}

fun aWordDetailState(
    word: AsyncData<Word> = AsyncData.Success(aWord()),
) = State(word = word)
