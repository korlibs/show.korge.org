package snake.scene

import korlibs.datastructure.IntArray2
import korlibs.datastructure.Observable
import korlibs.datastructure.observe
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.format.ASE
import korlibs.image.format.readBitmap
import korlibs.image.text.TextAlignment
import korlibs.image.tiles.*
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.keys
import korlibs.korge.scene.PixelatedScene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.time.interval
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.korge.view.tiles.tileMap
import korlibs.math.geom.*
import korlibs.math.geom.slice.splitInRows
import korlibs.math.random.*
import snake.model.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class IngameScene : PixelatedScene(32 * 16, 32 * 16, sceneSmoothing = true), StateScene by StateScene.Mixin() {
    lateinit var tilesBmp: Bitmap32
    val tiles by lazy { tilesBmp.slice().splitInRows(16, 16) }
    val tileSet by lazy { TileSet.fromBitmapSlices(16, 16, tiles, border = 0) }
    val tileMap by lazy { sceneView.tileMap(TileMapData(32, 32, tileSet = tileSet)) }
    val tileMapRules by lazy { CombinedRuleMatcher(WallProvider, AppleProvider) }
    val intMap by lazy { IntArray2(tileMap.map.width, tileMap.map.height, EMPTY).observe {
        IntGridToTileGrid(this.base as IntArray2, tileMapRules, tileMap.map, it)
    } }
    var hiScoreBeatShown = false

    val initialGameInfo = GameInfo(score = 0)
    val initialSnake = Snake(listOf(PointInt(5, 5), PointInt(6, 5)), length = 4)
    var gameInfo: GameInfo by Observable(initialGameInfo, after = {
        //updateGameInfo(it)
        sceneView.dispatch(GameInfoUpdatedEvent(it))
    })
    var snake = initialSnake
    val random = Random(0)

    override suspend fun SContainer.sceneInit() {
        tilesBmp = resourcesVfs["gfx/tiles.ase"].readBitmap(ASE).toBMP32IfRequired()
        tileMap
        sceneContainer().changeTo { OverlayScene() }
    }

    override suspend fun SContainer.sceneMain() {
        runStates(::ingame)
    }

    suspend fun ingame(view: Container) {
        intMap[RectangleInt(0, 0, intMap.width, intMap.height)] = EMPTY
        intMap[RectangleInt(0, 0, intMap.width, 1)] = WALL
        intMap[RectangleInt(0, intMap.height - 1, intMap.width, 1)] = WALL
        intMap[RectangleInt(0, 0, 1, intMap.height)] = WALL
        intMap[RectangleInt(intMap.width - 1, 0, 1, intMap.height)] = WALL
        intMap[RectangleInt(8, 8, 3, 3)] = WALL
        intMap[RectangleInt(16, 20, 4, 10)] = WALL

        hiScoreBeatShown = false
        gameInfo = gameInfo.copy(score = 0)
        snake = initialSnake
        addRandomApple()
        renderSnake()

        sceneView.dispatch(GameStartEvent())

        intMap[4, 3] = WALL
        intMap[4, 4] = WALL
        intMap[3, 4] = WALL
        intMap[4, 5] = WALL
        //intMap[4, 4] = WALL

        var direction = SnakeDirection.RIGHT

        view.interval(0.1.seconds) {
            moveSnake(direction)
        }

        view.keys {
            down(Key.UP) { direction = SnakeDirection.UP }
            down(Key.DOWN) { direction = SnakeDirection.DOWN }
            down(Key.LEFT) { direction = SnakeDirection.LEFT }
            down(Key.RIGHT) { direction = SnakeDirection.RIGHT }
            down(Key.SPACE) {
                view.speed = if (view.speed == 0.0) 1.0 else 0.0
            }
        }
    }

    suspend fun gameOver(view: Container) = with(view) {
        //val views = view.stage!!.view
        val background = solidRect(width, height, Colors.BLACK).alpha(0.0)

        val text = text("GAME OVER", textSize = 1.0, alignment = TextAlignment.CENTER).xy(width * 0.5, height * 0.5)
        speed = 0.0

        keys {
            down {
                change(::ingame)
            }
        }

        tween(
            text::textSize[64.0].easeOut(),
            background::alpha[0.5],
            time = 0.5.seconds,
        )
    }

    fun renderSnake() {
        snake.render(tileMap.map, intMap)
    }

    fun addRandomApple() {
        while (true) {
            val point: Point = random[Rectangle(0, 0, intMap.width - 0.5, intMap.height - 0.5)]
            val ipoint = point.toInt()
            if (intMap[ipoint] == EMPTY) {
                intMap[ipoint] = APPLE
                return
            }
        }
    }

    fun moveSnake(dir: SnakeDirection) {
        val oldSnake = snake
        snake = snake.withMove(dir)
        val headValue = intMap[snake.headPos]
        var isApple = headValue == APPLE
        when {
            isApple -> {
                snake = snake.copy(length = snake.length + 1)
                gameInfo = gameInfo.withIncrementedScore(+1)
            }
            headValue != EMPTY -> {
                change(::gameOver)
                return
            }
        }
        oldSnake.clear(tileMap.map, intMap)
        snake.render(tileMap.map, intMap)
        if (isApple) {
            addRandomApple()
            //println(intMap.base)
        }
        //println(intMap.base)
    }
}
