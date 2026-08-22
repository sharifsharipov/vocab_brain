package uz.sharif.vocabbrain.feature.review

import uz.sharif.vocabbrain.feature.review.domain.model.ReviewScheduler
import uz.sharif.vocabbrain.feature.review.domain.model.ReviewState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReviewSchedulerTest {

    private val now = 1_000_000L
    private val fresh = ReviewState(
        repetitions = 0,
        easeFactor = 2.5,
        intervalDays = 0,
        dueAtMillis = 0L,
    )

    @Test
    fun `a first correct answer schedules the word for tomorrow`() {
        val next = ReviewScheduler.next(fresh, wasCorrect = true, nowMillis = now)

        assertThat(next.repetitions).isEqualTo(1)
        assertThat(next.intervalDays).isEqualTo(1)
        assertThat(next.dueAtMillis).isEqualTo(now + ReviewScheduler.DAY_MILLIS)
    }

    @Test
    fun `intervals widen as the streak grows`() {
        var state = fresh
        val intervals = List(4) {
            state = ReviewScheduler.next(state, wasCorrect = true, nowMillis = now)
            state.intervalDays
        }

        assertThat(intervals).containsExactly(1, 3, 8, 22).inOrder()
    }

    @Test
    fun `a wrong answer resets the streak and brings the word back tomorrow`() {
        val learned = ReviewState(
            repetitions = 4,
            easeFactor = 2.5,
            intervalDays = 20,
            dueAtMillis = now,
        )

        val next = ReviewScheduler.next(learned, wasCorrect = false, nowMillis = now)

        assertThat(next.repetitions).isEqualTo(0)
        assertThat(next.intervalDays).isEqualTo(1)
        assertThat(next.easeFactor).isWithin(1e-9).of(2.3)
    }

    @Test
    fun `ease stays inside its bounds`() {
        var hard = fresh.copy(easeFactor = ReviewScheduler.MIN_EASE_FACTOR)
        repeat(3) { hard = ReviewScheduler.next(hard, wasCorrect = false, nowMillis = now) }
        assertThat(hard.easeFactor).isEqualTo(ReviewScheduler.MIN_EASE_FACTOR)

        var easy = fresh
        repeat(10) { easy = ReviewScheduler.next(easy, wasCorrect = true, nowMillis = now) }
        assertThat(easy.easeFactor).isAtMost(ReviewScheduler.MAX_EASE_FACTOR)
    }
}
