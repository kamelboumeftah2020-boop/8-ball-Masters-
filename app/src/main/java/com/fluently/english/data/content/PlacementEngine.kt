package com.fluently.english.data.content

/**
 * Adaptive, multi-stage placement test (the approach used by the Oxford and
 * Cambridge online placement tests): the candidate starts at A2 and answers a
 * block of five items. A strong block moves them up a level, a weak one ends the
 * test (or moves down from the starting level), and a borderline score triggers
 * a second block at the same level.
 */
class PlacementEngine(private val bank: List<PlacementItem> = PlacementBank) {

    private val blocks: Map<CefrLevel, List<List<PlacementItem>>> =
        bank.groupBy { it.level }.mapValues { (_, items) -> items.chunked(BLOCK_SIZE) }

    private val attempts = mutableMapOf<CefrLevel, MutableList<Int>>()
    private val verdicts = mutableMapOf<CefrLevel, Boolean>()
    private val skillCorrect = mutableMapOf<Skill, Int>()
    private val skillTotal = mutableMapOf<Skill, Int>()

    var currentLevel: CefrLevel = START_LEVEL
        private set
    var finished: Boolean = false
        private set
    var answered: Int = 0
        private set

    fun currentBlock(): List<PlacementItem> {
        val tries = attempts[currentLevel]?.size ?: 0
        return blocks.getValue(currentLevel)[tries]
    }

    /** Records one block's answers (in the order of [currentBlock]). */
    fun submit(results: List<Boolean>) {
        check(!finished)
        val block = currentBlock()
        require(results.size == block.size)
        block.zip(results).forEach { (item, ok) ->
            skillTotal.merge(item.skill, 1, Int::plus)
            if (ok) skillCorrect.merge(item.skill, 1, Int::plus)
        }
        answered += results.size
        val score = results.count { it }
        val levelAttempts = attempts.getOrPut(currentLevel) { mutableListOf() }
        levelAttempts += score

        val verdict: Boolean? = when (levelAttempts.size) {
            1 -> when {
                score >= PASS_SINGLE -> true
                score >= BORDERLINE -> null
                else -> false
            }
            else -> levelAttempts.sum() >= PASS_DOUBLE
        }
        if (verdict == null) return // second block at the same level

        verdicts[currentLevel] = verdict
        val nextLevel = if (verdict) currentLevel.next else currentLevel.previous
        if (nextLevel == null || verdicts.containsKey(nextLevel) || (!verdict && currentLevel != START_LEVEL)) {
            finished = true
        } else {
            currentLevel = nextLevel
        }
    }

    /** Highest level passed, or null when the candidate is below A1 (start from zero). */
    val result: CefrLevel?
        get() = verdicts.filterValues { it }.keys.maxByOrNull { it.ordinal }

    /** The course the candidate should start: the first level they have not mastered. */
    val recommendedCourse: CefrLevel
        get() = result?.let { it.next ?: it } ?: CefrLevel.A1

    fun skillPercent(skill: Skill): Int? {
        val total = skillTotal[skill] ?: return null
        return (skillCorrect[skill] ?: 0) * 100 / total
    }

    companion object {
        const val BLOCK_SIZE = 5
        const val PASS_SINGLE = 4
        const val BORDERLINE = 3
        const val PASS_DOUBLE = 7
        val START_LEVEL = CefrLevel.A2
    }
}
