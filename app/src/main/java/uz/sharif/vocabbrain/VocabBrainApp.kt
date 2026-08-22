package uz.sharif.vocabbrain

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.context.GlobalContext
import uz.sharif.vocabbrain.di.appModules
import uz.sharif.vocabbrain.feature.sync.domain.SyncWordsUseCase

class VocabBrainApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@VocabBrainApp)
            modules(appModules)
        }

        // Provider differs per build type; see src/debug and src/release.
        installAppCheckProvider()
        syncVocabulary()
    }

    /** Best-effort: a failed sync leaves the local database untouched and fully usable. */
    private fun syncVocabulary() {
        val syncWords: SyncWordsUseCase = GlobalContext.get().get()
        applicationScope.launch { syncWords() }
    }
}
