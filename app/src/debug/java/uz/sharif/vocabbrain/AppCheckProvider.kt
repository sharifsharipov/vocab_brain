package uz.sharif.vocabbrain

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds attest with a token printed to Logcat on first run; register that token in
 * the Firebase console once per machine, otherwise AI calls are rejected.
 */
fun installAppCheckProvider() {
    Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
}
