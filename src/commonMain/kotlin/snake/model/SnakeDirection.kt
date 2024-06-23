package snake.model

import korlibs.math.geom.*

enum class SnakeDirection(
    val delta: Vector2I,
    val isHorizontal: Boolean?,
) {
    UP(Vector2I(0, -1), isHorizontal = false),
    DOWN(Vector2I(0, +1), isHorizontal = false),
    LEFT(Vector2I(-1, 0), isHorizontal = true),
    RIGHT(Vector2I(+1, 0), isHorizontal = true),
    NONE(Vector2I(0, 0), isHorizontal = null),
    ;

    companion object {
        fun fromPoints(old: PointInt, new: PointInt): SnakeDirection = when {
            new.x > old.x -> RIGHT
            new.x < old.x -> LEFT
            new.y > old.y -> DOWN
            new.y < old.y -> UP
            else -> NONE
        }
    }
}
