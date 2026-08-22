package uz.sharif.vocabbrain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportRoute
import uz.sharif.vocabbrain.feature.importvocab.presentation.ImportViewModel
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizRoute
import uz.sharif.vocabbrain.feature.quiz.presentation.QuizViewModel
import uz.sharif.vocabbrain.feature.result.presentation.ResultRoute
import uz.sharif.vocabbrain.feature.result.presentation.ResultViewModel
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailRoute
import uz.sharif.vocabbrain.feature.word.presentation.detail.WordDetailViewModel
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListRoute
import uz.sharif.vocabbrain.feature.word.presentation.list.WordListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun VocabNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Words) {

        composable<Screen.Words> {
            val viewModel: WordListViewModel = koinViewModel()
            WordListRoute(
                viewModel = viewModel,
                onNavigateToDetail = { wordId -> navController.navigate(Screen.WordDetail(wordId)) },
                onNavigateToImport = { navController.navigate(Screen.Import) },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz) },
            )
        }

        composable<Screen.WordDetail> { entry ->
            val wordId = entry.toRoute<Screen.WordDetail>().wordId
            val viewModel: WordDetailViewModel = koinViewModel { parametersOf(wordId) }
            WordDetailRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Import> {
            val viewModel: ImportViewModel = koinViewModel()
            ImportRoute(
                viewModel = viewModel,
                // The import screen is done once its quiz starts, so it leaves the back stack.
                onNavigateToQuiz = {
                    navController.navigate(Screen.Quiz) {
                        popUpTo(Screen.Import) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Quiz> {
            val viewModel: QuizViewModel = koinViewModel()
            QuizRoute(
                viewModel = viewModel,
                // A finished quiz is replaced by its result: going back must not replay it.
                onNavigateToResult = {
                    navController.navigate(Screen.Result) {
                        popUpTo(Screen.Quiz) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Result> {
            val viewModel: ResultViewModel = koinViewModel()
            ResultRoute(
                viewModel = viewModel,
                onNavigateToQuiz = {
                    navController.navigate(Screen.Quiz) {
                        popUpTo(Screen.Result) { inclusive = true }
                    }
                },
                onNavigateToWords = {
                    navController.navigate(Screen.Words) {
                        popUpTo(Screen.Words) { inclusive = true }
                    }
                },
            )
        }
    }
}
