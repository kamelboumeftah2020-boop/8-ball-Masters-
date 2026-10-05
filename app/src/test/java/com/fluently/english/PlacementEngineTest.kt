package com.fluently.english

import com.fluently.english.data.content.CefrLevel
import com.fluently.english.data.content.PlacementEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementEngineTest {

    /** Simulates a candidate who answers correctly up to (and including) [trueLevel]. */
    private fun run(trueLevel: CefrLevel?): PlacementEngine {
        val engine = PlacementEngine()
        var guard = 0
        while (!engine.finished) {
            val block = engine.currentBlock()
            val knows = trueLevel != null && block.first().level.ordinal <= trueLevel.ordinal
            engine.submit(List(block.size) { knows })
            check(++guard < 50)
        }
        return engine
    }

    @Test
    fun placesEveryLevelCorrectly() {
        CefrLevel.entries.forEach { level ->
            val engine = run(level)
            assertEquals(level, engine.result)
            assertEquals(level.next ?: level, engine.recommendedCourse)
        }
    }

    @Test
    fun completeBeginnerStartsAtA1() {
        val engine = run(null)
        assertNull(engine.result)
        assertEquals(CefrLevel.A1, engine.recommendedCourse)
        assertEquals(10, engine.answered) // A2 block, then A1 block
    }

    @Test
    fun borderlineScoreTriggersSecondBlock() {
        val engine = PlacementEngine()
        engine.submit(listOf(true, true, true, false, false)) // 3/5 at A2
        assertEquals(CefrLevel.A2, engine.currentLevel)
        engine.submit(listOf(true, true, true, true, false)) // 7/10 overall → pass
        assertEquals(CefrLevel.B1, engine.currentLevel)
    }

    @Test
    fun skillBreakdownIsReported() {
        val engine = run(CefrLevel.B2)
        assertTrue(engine.skillPercent(com.fluently.english.data.content.Skill.LISTENING) != null)
    }
}
