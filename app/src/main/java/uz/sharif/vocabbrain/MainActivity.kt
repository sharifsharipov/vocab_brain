package uz.sharif.vocabbrain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uz.sharif.vocabbrain.core.ui.theme.VocabbrainTheme
import uz.sharif.vocabbrain.navigation.VocabNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocabbrainTheme {
                VocabNavHost()
            }
        }
    }
}
