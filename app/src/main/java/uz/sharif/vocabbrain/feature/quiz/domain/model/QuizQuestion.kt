package uz.sharif.vocabbrain.feature.quiz.domain.model

enum class QuestionType { MULTIPLE_CHOICE, WRITING }

data class QuizQuestion(
    val id: Int,
    val type: QuestionType,
    val targetWord: String,
    val prompt: String,
    /** Four Uzbek options for multiple choice; empty for writing questions. */
    val options: List<String>,
    val correctAnswer: String,
    /** Writing questions only, e.g. "Bosh harfi: R (8 ta harf)". */
    val hint: String?,
    val explanation: String,
) {
    /** Writing answers are compared loosely: case and surrounding spaces do not count. */
    fun isCorrect(answer: String): Boolean = when (type) {
        QuestionType.MULTIPLE_CHOICE -> answer == correctAnswer
        QuestionType.WRITING -> answer.trim().equals(correctAnswer.trim(), ignoreCase = true)
    }
}

data class QuizSettings(
    val totalQuestions: Int,
    val timePerQuestionSeconds: Int,
)

data class Quiz(
    val settings: QuizSettings,
    val questions: List<QuizQuestion>,
)

/** What the learner asks for before a text is analysed. */
data class QuizConfig(
    val questionCount: Int = DEFAULT_QUESTION_COUNT,
    val questionTypes: Set<QuestionType> = QuestionType.entries.toSet(),
    val timePerQuestionSeconds: Int = DEFAULT_TIME_PER_QUESTION,
) {
    init {
        require(questionTypes.isNotEmpty()) { "A quiz needs at least one question type" }
    }

    companion object {
        const val DEFAULT_QUESTION_COUNT = 10
        const val DEFAULT_TIME_PER_QUESTION = 30
        val QUESTION_COUNT_CHOICES = listOf(5, 10, 20)
        val TIME_CHOICES = listOf(15, 30, 60)
    }
}
