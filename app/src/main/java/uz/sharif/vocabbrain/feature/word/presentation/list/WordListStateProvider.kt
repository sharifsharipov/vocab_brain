package uz.sharif.vocabbrain.feature.word.presentation.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uz.sharif.vocabbrain.core.architecture.AsyncData
import uz.sharif.vocabbrain.feature.word.domain.model.Word
import uz.sharif.vocabbrain.feature.word.domain.model.aWordList
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListContract.State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Every state the screen can be in, so previews cover all of them. */
class WordListStateProvider : PreviewParameterProvider<State> {
    override val values: Sequence<State>
        get() = sequenceOf(
            aWordListState(),
            aWordListState(words = AsyncData.Loading()),
            aWordListState(words = AsyncData.Failure(Exception("Network unreachable"))),
            aWordListState(query = "can"),
            aWordListState(words = AsyncData.Success(persistentListOf())),
        )
}

fun aWordListState(
    words: AsyncData<ImmutableList<Word>> = AsyncData.Success(aWordList().toImmutableList()),
    query: String = "",
) = State(words = words, query = query)
