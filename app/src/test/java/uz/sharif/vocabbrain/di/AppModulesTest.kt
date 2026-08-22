package uz.sharif.vocabbrain.di

import android.content.Context
import uz.sharif.vocabbrain.core.database.AppDatabase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Fails the build when a Koin definition is missing a dependency, instead of crashing the
 * app the first time that screen is opened. Android, Room and Firebase types are declared
 * as external because they are built by the framework, not by a constructor Koin can see.
 */
@OptIn(KoinExperimentalAPI::class)
class AppModulesTest {

    @Test
    fun `every definition can be built`() {
        val external = listOf(
            Context::class,
            AppDatabase::class,

            GenerativeModel::class,
            FirebaseAuth::class,
            FirebaseFirestore::class,
        )
        // Verified as one graph: a use case in one module may depend on a repository in
        // another. firebaseModule is excluded because its objects come from the SDK, so
        // there is no constructor for verification to walk — its types are external here.
        val ownModules = appModules - firebaseModule
        module { includes(ownModules) }.verify(extraTypes = external, injections = null)
    }
}
