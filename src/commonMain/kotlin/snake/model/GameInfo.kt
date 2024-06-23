package snake.model

data class GameInfo(
    val score: Int = 0,
    val hiScore: Int = 0,
) {
    fun withIncrementedScore(increment: Int = +1): GameInfo {
        val newScore = score + increment
        return GameInfo(score = newScore, hiScore = maxOf(hiScore, newScore))
    }
}
