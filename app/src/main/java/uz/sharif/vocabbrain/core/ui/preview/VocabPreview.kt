package uz.sharif.vocabbrain.core.ui.preview

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uz.sharif.vocabbrain.core.ui.theme.VocabbrainTheme

@Composable
fun VocabPreview(content: @Composable () -> Unit) {
    VocabbrainTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            content()
        }
    }
}
