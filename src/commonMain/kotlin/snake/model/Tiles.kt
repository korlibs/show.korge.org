package snake.model

import korlibs.image.tiles.*

val EMPTY = 0
val APPLE = 1
val WALL = 2
val SNAKE = 3

object AppleProvider : ISimpleTileProvider by (SimpleTileProvider(value = APPLE).also {
    it.rule(SimpleRule(Tile(12)))
})

object WallProvider : ISimpleTileProvider by (SimpleTileProvider(value = WALL).also {
    it.rule(SimpleRule(Tile(16)))
    it.rule(SimpleRule(Tile(17), right = true))
    it.rule(SimpleRule(Tile(18), left = true, right = true))
    it.rule(SimpleRule(Tile(19), left = true, down = true))
    it.rule(SimpleRule(Tile(20), left = true, up = true, down = true))
    it.rule(SimpleRule(Tile(21), left = true, up = true, down = true, right = true))
})

object SnakeProvider : ISimpleTileProvider by (SimpleTileProvider(value = SNAKE).also {
    it.rule(SimpleRule(Tile(0)))
    it.rule(SimpleRule(Tile(1), right = true))
    it.rule(SimpleRule(Tile(2), left = true, right = true))
    it.rule(SimpleRule(Tile(3), left = true, down = true))
})

object SnakeHeadProvider : ISimpleTileProvider by (SimpleTileProvider(value = SNAKE).also {
    it.rule(SimpleRule(Tile(0)))
    it.rule(SimpleRule(Tile(4), right = true))
})
