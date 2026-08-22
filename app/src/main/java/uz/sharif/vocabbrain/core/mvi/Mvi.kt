package uz.sharif.vocabbrain.core.mvi

/** Immutable snapshot a screen renders. One state object per screen. */
interface UiState

/** User or system input a screen sends to its ViewModel. */
interface UiIntent

/** One-shot event a screen consumes exactly once (navigation, snackbar, toast). */
interface UiEffect
