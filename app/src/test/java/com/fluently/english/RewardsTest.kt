package com.fluently.english

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fluently.english.data.progress.Progress
import com.fluently.english.data.progress.ProgressRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class RewardsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before fun clear() {
        context.getSharedPreferences("progress", Context.MODE_PRIVATE).edit().clear().commit()
    }

    /** Saves [p] as if the app last ran on an earlier day, then "opens" it on [today]. */
    private fun reopen(p: Progress, today: Long): ProgressRepository {
        ProgressRepository(context) { p.lastActiveDay }.replace(p)
        return ProgressRepository(context) { today }
    }

    @Test fun oneMissedDayCanBeRestoredAndContinues() {
        val repo = reopen(Progress(onboarded = true, streak = 6, bestStreak = 6, lastActiveDay = 98), today = 100)
        assertEquals(0, repo.progress.value.streak)
        assertTrue(repo.progress.value.canRestoreStreak(100))
        assertTrue(repo.restoreStreak())
        assertEquals(6, repo.progress.value.streak)
        repo.addBonusXp(10) // studying today continues the restored streak
        assertEquals(7, repo.progress.value.streak)
        assertFalse(repo.progress.value.canRestoreStreak(100))
    }

    @Test fun twoMissedDaysCannotBeRestored() {
        val repo = reopen(Progress(onboarded = true, streak = 6, lastActiveDay = 97), today = 100)
        assertFalse(repo.progress.value.canRestoreStreak(100))
        assertFalse(repo.restoreStreak())
    }

    @Test fun offerExpiresTheNextDay() {
        reopen(Progress(onboarded = true, streak = 6, lastActiveDay = 98), today = 100)
        val tomorrow = ProgressRepository(context) { 101 }
        assertFalse(tomorrow.progress.value.canRestoreStreak(101))
    }

    @Test fun bonusXpIsAdded() {
        val repo = ProgressRepository(context) { 200 }
        repo.addBonusXp(40)
        assertEquals(40, repo.progress.value.xp)
        assertEquals(40, repo.progress.value.dayXp[200L])
    }
}
