package uz.sharif.vocabbrain.navigation

import kotlinx.serialization.Serializable

/** Type-safe routes: a destination is a value, so arguments are checked by the compiler. */
@Serializable
sealed interface Screen {
    @Serializable data object Words : Screen
    @Serializable data class WordDetail(val wordId: String) : Screen
    @Serializable data object Import : Screen
    @Serializable data object Quiz : Screen
    @Serializable data object Result : Screen
}
