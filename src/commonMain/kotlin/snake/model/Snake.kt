package snake.model

import korlibs.datastructure.*
import korlibs.image.tiles.*
import korlibs.math.geom.*

data class Snake(val list: List<PointInt>, val length: Int) {
    val headPos get() = list.last()

    fun withMove(dir: SnakeDirection): Snake =
        Snake(
            (list + (list.last() + dir.delta)).takeLast(length),
            length = length
        )

    fun clear(map: TileMapData, intMap: IntIArray2) {
        for ((index, point) in list.withIndex()) {
            intMap[point] = EMPTY
            if (map.inside(point.x, point.y)) {
                while (map.getStackLevel(point.x, point.y) > 0) {
                    map.removeLast(point.x, point.y)
                }
            }
        }
    }

    fun render(map: TileMapData, intMap: IntIArray2) {
        //println("-----")
        for (point in list) {
            intMap[point] = SNAKE
        }

        for ((index, point) in list.withIndex()) {
            val isHead = index == list.size - 1
            val prevPoint = list.getOrElse(index - 1) { point }
            val nextPoint = list.getOrElse(index + 1) { point }
            val dir = SnakeDirection.fromPoints(prevPoint, point)
            val ndir = SnakeDirection.fromPoints(point, nextPoint)

            val tile = when {
                isHead -> SnakeHeadProvider.get(
                    SimpleTileSpec(
                        left = dir == SnakeDirection.LEFT,
                        up = dir == SnakeDirection.UP,
                        right = dir == SnakeDirection.RIGHT,
                        down = dir == SnakeDirection.DOWN,
                    )
                )
                else -> SnakeProvider.get(
                    SimpleTileSpec(
                        left = dir == SnakeDirection.RIGHT || ndir == SnakeDirection.LEFT,
                        up = dir == SnakeDirection.DOWN || ndir == SnakeDirection.UP,
                        right = dir == SnakeDirection.LEFT || ndir == SnakeDirection.RIGHT,
                        down = dir == SnakeDirection.UP || ndir == SnakeDirection.DOWN,
                    )
                )
            }
            map.push(point.x, point.y, tile)
        }
    }
}
