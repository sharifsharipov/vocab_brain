package uz.sharif.vocabbrain.feature.sync.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Every device gets an anonymous account on first run, so the vocabulary syncs without a
 * sign-up screen. Linking that account to a real sign-in later keeps the same data.
 */
class AuthManager(private val auth: FirebaseAuth) {

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun requireUid(): String =
        currentUid ?: auth.signInAnonymously().await().user?.uid
        ?: error("Firebase hisobini ochib bo'lmadi")
}
