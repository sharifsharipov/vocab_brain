package uz.sharif.vocabbrain.core.time


fun interface TimeProvider {
    fun nowMillis(): Long

    companion object {
        val System = TimeProvider { java.lang.System.currentTimeMillis() }
    }
}
