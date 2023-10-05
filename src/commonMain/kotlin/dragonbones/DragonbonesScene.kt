package dragonbones

import com.dragonbones.event.*
import korlibs.time.*
import korlibs.korge.dragonbones.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.time.*
import korlibs.korge.view.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.io.serialization.json.*
import korlibs.math.geom.*
import korlibs.math.random.*
import extension.*
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.printStackTrace
import kotlin.random.*

open class DragonbonesScene : ShowScene() {
    override suspend fun SContainer.sceneMain() {
        views.injector
            .mapPrototype { HelloScene() }
            .mapPrototype { MyScene() }
            .mapPrototype { ClassicDragonScene() }
            .mapPrototype { EyeTrackingScene() }
            .mapPrototype { HelloWorldScene() }
            .mapPrototype { SkinChangingScene() }

        sceneContainer().changeTo<MyScene>()
    }
}


private class HelloScene : Scene() {
    override suspend fun SContainer.sceneInit() {
        println("HelloScene.sceneInit[0]")
        solidRect(100, 100, Colors.RED) {
            position(100, 100)
            alpha(0.5)
            mouse {
                over {
                    alpha(1.0)
                }
                out {
                    alpha(0.5)
                }
            }
        }
        println("HelloScene.sceneInit[1]")
    }
}

private abstract class MyBaseScene : Scene() {
}

private class MyScene : MyBaseScene() {
    lateinit var buttonContainer: Container

    override suspend fun SContainer.sceneInit() {
        //addEventListener<MouseEvent> {
        //	println("MouseEvent: ${views.nativeWidth},${views.nativeHeight} :: ${views.virtualWidth},${views.virtualHeight} :: $it")
        //}

        val mySceneContainer = sceneContainer(views) {
            this.x = views.virtualWidth.toDouble() * 0.5
            this.y = views.virtualHeight.toDouble() * 0.5
        }
        buttonContainer = this
        this += Button("Hello") {
            println("Hello")
            mySceneContainer.changeToDisablingButtons<HelloWorldScene>()
        }.position(8, views.virtualHeight - 48)
        //this += Button("Classic") { mySceneContainer.changeToDisablingButtons<ClassicDragonScene>() }.position(108, views.virtualHeight - 48)
        this += Button("Eye Tracking") {
            println("Eye Tracking")
            mySceneContainer.changeToDisablingButtons<EyeTrackingScene>()
        }.position(200, views.virtualHeight - 48)
        this += Button("Skin Changing") {
            println("Skin Changing")
            mySceneContainer.changeToDisablingButtons<SkinChangingScene>()
        }.position(600, views.virtualHeight - 48)
        mySceneContainer.changeToDisablingButtons<HelloWorldScene>()
    }

    suspend inline fun <reified T : Scene> SceneContainer.changeToDisablingButtons() {
        for (child in buttonContainer.children.filterIsInstance<Button>()) {
            //println("DISABLE BUTTON: $child")
            child.enabledButton = false
        }
        try {
            changeTo<T>()
        } finally {
            for (child in buttonContainer.children.filterIsInstance<Button>()) {
                //println("ENABLE BUTTON: $child")
                child.enabledButton = true
            }
        }
    }
}


private class HelloWorldScene : BaseDbScene() {
    val SCALE = 1.6
    override suspend fun SContainer.sceneInit() {
        val skeDeferred = asyncImmediately { Json.parse(res["mecha_1002_101d_show/mecha_1002_101d_show_ske.json"].readString())!! }
        //val skeDeferred = asyncImmediately { MemBufferWrap(resources["mecha_1002_101d_show/mecha_1002_101d_show_ske.dbbin"].readBytes()) }
        val texDeferred = asyncImmediately { res["mecha_1002_101d_show/mecha_1002_101d_show_tex.json"].readString() }
        val imgDeferred = asyncImmediately { res["mecha_1002_101d_show/mecha_1002_101d_show_tex.png"].readBitmap().mipmaps() }

        val data = factory.parseDragonBonesData(skeDeferred.await())
        val atlas = factory.parseTextureAtlasData(Json.parse(texDeferred.await())!!, imgDeferred.await())

        val armatureDisplay = factory.buildArmatureDisplay("mecha_1002_101d")!!.position(0, 300).scale(SCALE)

        //armatureDisplay.animation.play("walk")
        println(armatureDisplay.animation.animationNames)
        //armatureDisplay.animation.play("jump")
        armatureDisplay.animation.play("idle")
        //scaleView(512, 512) {
        this += armatureDisplay
        //}
    }
}

private class ClassicDragonScene : BaseDbScene() {
    override suspend fun SContainer.sceneInit() {
        //val scale = 0.3
        val scale = 0.8
        val ske = asyncImmediately { res["Dragon/Dragon_ske.json"].readString() }
        val tex = asyncImmediately { res["Dragon/Dragon_tex.json"].readString() }
        val img = asyncImmediately { res["Dragon/Dragon_tex.png"].readBitmap() }

        val data = factory.parseDragonBonesData(Json.parse(ske.await())!!)

        val atlas = factory.parseTextureAtlasData(
            Json.parse(tex.await())!!,
            img.await()
        )
        val armatureDisplay = factory.buildArmatureDisplay("Dragon", "Dragon")!!.position(0, 200).scale(scale)
        armatureDisplay.animation.play("walk")
        println(armatureDisplay.animation.animationNames)
        //armatureDisplay.animation.play("jump")
        //armatureDisplay.animation.play("fall")
        this += armatureDisplay
    }
}


private class EyeTrackingScene : BaseDbScene() {
    val scale = 0.46f
    var totalTime = 0.0f

    override suspend fun SContainer.sceneInit() {
        try {
            println("EyeTrackingScene[0]")

            val _animationNames = listOf(
                "PARAM_ANGLE_X", "PARAM_ANGLE_Y", "PARAM_ANGLE_Z",
                "PARAM_EYE_BALL_X", "PARAM_EYE_BALL_Y",
                "PARAM_BODY_X", "PARAM_BODY_Y", "PARAM_BODY_Z",
                "PARAM_BODY_ANGLE_X", "PARAM_BODY_ANGLE_Y", "PARAM_BODY_ANGLE_Z",
                "PARAM_BREATH"
            )

            val skeDeferred = asyncImmediately { res["shizuku/shizuku_ske.json"].readString() }
            val tex00Deferred = asyncImmediately { res["shizuku/shizuku.1024/texture_00.png"].readBitmap().mipmaps() }
            val tex01Deferred = asyncImmediately { res["shizuku/shizuku.1024/texture_01.png"].readBitmap().mipmaps() }
            val tex02Deferred = asyncImmediately { res["shizuku/shizuku.1024/texture_02.png"].readBitmap().mipmaps() }
            val tex03Deferred = asyncImmediately { res["shizuku/shizuku.1024/texture_03.png"].readBitmap().mipmaps() }

            println("EyeTrackingScene[1]")

            factory.parseDragonBonesData(
                Json.parse(skeDeferred.await())!!,
                "shizuku"
            )
            println("EyeTrackingScene[2]")
            factory.updateTextureAtlases(
                arrayOf(
                    tex00Deferred.await(),
                    tex01Deferred.await(),
                    tex02Deferred.await(),
                    tex03Deferred.await()
                ), "shizuku"
            )
            println("EyeTrackingScene[3]")
            val armatureDisplay = factory.buildArmatureDisplay("shizuku", "shizuku")!!
                .position(0, 300).scale(this@EyeTrackingScene.scale)
            this += armatureDisplay

            println(armatureDisplay.animation.animationNames)
            println("EyeTrackingScene[4]")
            //armatureDisplay.play("idle_00")
            armatureDisplay.animation.play("idle_00")

            var target = Point()
            var ftarget = Point()

            mouse {
                moveAnywhere {
                    val lm = localMousePos(views)
                    ftarget = (lm - armatureDisplay.pos) / this@EyeTrackingScene.scale
                    //println(":" + localMouseXY(views) + ", " + target + " :: ${armatureDisplay.x}, ${armatureDisplay.y} :: ${this@EyeTrackingScene.scale}")
                }
                exit {
                    ftarget = Point(
                        armatureDisplay.x / this@EyeTrackingScene.scale,
                        (armatureDisplay.y - 650) / this@EyeTrackingScene.scale
                    )
                    //println(":onExit:" + " :: $target :: ${armatureDisplay.x}, ${armatureDisplay.y} :: ${this@EyeTrackingScene.scale}")
                }
            }

            // This job will be automatically destroyed by the SceneContainer
            launchImmediately {
                val bendRatio = 0.75
                val ibendRatio = 1.0 - bendRatio
                while (true) {
                    target = target * bendRatio + ftarget * ibendRatio
                    delay(16.milliseconds)
                }
            }

            addUpdater {
                totalTime += it.milliseconds.toFloat()

                val armature = armatureDisplay.armature
                val animation = armatureDisplay.animation
                val canvas = armature.armatureData.canvas!!

                var p = 0.0
                val pX = max(min((target.x - canvas.x) / (canvas.width * 0.5), 1.0), -1.0)
                val pY = -max(min((target.y - canvas.y) / (canvas.height * 0.5), 1.0), -1.0)
                for (animationName in _animationNames) {
                    if (!animation.hasAnimation(animationName)) {
                        continue
                    }

                    var animationState = animation.getState(animationName, 1)
                    if (animationState == null) {
                        animationState = animation.fadeIn(animationName, 0.1, 1, 1, animationName)
                        if (animationState != null) {
                            animationState.resetToPose = false
                            animationState.stop()
                        }
                    }

                    if (animationState == null) {
                        continue
                    }

                    when (animationName) {
                        "PARAM_ANGLE_X", "PARAM_EYE_BALL_X" -> p = (pX + 1.0) * 0.5
                        "PARAM_ANGLE_Y", "PARAM_EYE_BALL_Y" -> p = (pY + 1.0) * 0.5
                        "PARAM_ANGLE_Z" -> p = (-pX * pY + 1.0) * 0.5
                        "PARAM_BODY_X", "PARAM_BODY_ANGLE_X" -> p = (pX + 1.0) * 0.5
                        "PARAM_BODY_Y", "PARAM_BODY_ANGLE_Y" -> p = (-pX * pY + 1.0) * 0.5
                        "PARAM_BODY_Z", "PARAM_BODY_ANGLE_Z" -> p = (-pX * pY + 1.0) * 0.5
                        "PARAM_BREATH" -> p = (sin(totalTime / 1000.0) + 1.0) * 0.5
                    }

                    animationState.currentTime = p * animationState.totalTime
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

private class SkinChangingScene : BaseDbScene() {
    val SCALE = 0.42
    val random = Random(0)

    override suspend fun SContainer.sceneInit() {
        val suitConfigs = listOf(
            listOf(
                "2010600a", "2010600a_1",
                "20208003", "20208003_1", "20208003_2", "20208003_3",
                "20405006",
                "20509005",
                "20703016", "20703016_1",
                "2080100c",
                "2080100e", "2080100e_1",
                "20803005",
                "2080500b", "2080500b_1"
            ),
            listOf(
                "20106010",
                "20106010_1",
                "20208006",
                "20208006_1",
                "20208006_2",
                "20208006_3",
                "2040600b",
                "2040600b_1",
                "20509007",
                "20703020",
                "20703020_1",
                "2080b003",
                "20801015"
            )
        )

        val deferreds = arrayListOf<Deferred<*>>()

        deferreds += asyncImmediately {
            factory.parseDragonBonesData(
                Json.parse(res["you_xin/body/body_ske.json"].readString())!!
            )
        }
        deferreds += asyncImmediately {
            val atlas = factory.parseTextureAtlasData(
                Json.parse(res["you_xin/body/body_tex.json"].readString())!!,
                res["you_xin/body/body_tex.png"].readBitmap().mipmaps()
            )
        }

        for ((i, suitConfig) in suitConfigs.withIndex()) {
            for (partArmatureName in suitConfig) {
                // resource/you_xin/suit1/2010600a/xxxxxx
                val path = "you_xin/" + "suit" + (i + 1) + "/" + partArmatureName + "/" + partArmatureName
                val dragonBonesJSONPath = path + "_ske.json"
                val textureAtlasJSONPath = path + "_tex.json"
                val textureAtlasPath = path + "_tex.png"
                //
                deferreds += asyncImmediately {
                    factory.parseDragonBonesData(Json.parse(res[dragonBonesJSONPath].readString())!!)
                    factory.parseTextureAtlasData(
                        Json.parse(res[textureAtlasJSONPath].readString())!!,
                        res[textureAtlasPath].readBitmap().mipmaps()
                    )
                }
            }
        }

        deferreds.awaitAll()

        val armatureDisplay = factory.buildArmatureDisplay("body")!!
            .position(0, 360).scale(SCALE)
        this += armatureDisplay

        println(armatureDisplay.animation.animationNames)
        //armatureDisplay.animation.play("idle_00")
        armatureDisplay.on(EventObject.LOOP_COMPLETE) {
            //println("LOOP!")
            // Random animation index.
            val nextAnimationName = random[armatureDisplay.animation.animationNames]
            armatureDisplay.animation.fadeIn(nextAnimationName, 0.3, 0)
        }
        armatureDisplay.animation.play("idle", 0)
        //armatureDisplay.animation.play("speak")

        for (part in suitConfigs[0]) {
            val partArmatureData = factory.getArmatureData(part)
            factory.replaceSkin(armatureDisplay.armature, partArmatureData!!.defaultSkin!!)
        }
        val _replaceSuitParts = arrayListOf<String>()
        var _replaceSuitIndex = 0

        mouse {
            onUpAnywhere {
                // This suit has been replaced, next suit.
                if (_replaceSuitParts.size == 0) {
                    _replaceSuitIndex++

                    if (_replaceSuitIndex >= suitConfigs.size) {
                        _replaceSuitIndex = 0
                    }

                    // Refill the unset parits.
                    for (partArmatureName in suitConfigs[_replaceSuitIndex]) {
                        _replaceSuitParts.add(partArmatureName)
                    }
                }

                // Random one part in this suit.
                val partIndex: Int = floor(random.nextDouble() * _replaceSuitParts.size).toInt()
                val partArmatureName = _replaceSuitParts[partIndex]
                val partArmatureData = factory.getArmatureData(partArmatureName)
                // Replace skin.
                factory.replaceSkin(armatureDisplay.armature, partArmatureData!!.defaultSkin!!)
                // Remove has been replaced
                _replaceSuitParts.removeAt(partIndex)
            }
        }
    }
}

private abstract class BaseDbScene : MyBaseScene() {
    val res get() = resourcesVfs["dragonbones"].jail()
    val factory = KorgeDbFactory()
}

private class Button(text: String, handler: suspend () -> Unit) : Container() {
    //val textField = TextOld(text, textSize = 32.0).apply { filtering = false }
    val textField = Text(text, textSize = 32.0).apply { smoothing = false }
    private val bounds = textField.textBounds
    val g = Graphics().apply {
        updateShape {
            fill(Colors.DARKGREY, 0.7) {
                roundRect(RoundRectangle(Rectangle(bounds.x, bounds.y, bounds.width + 16, bounds.height + 16), RectCorners(8.0, 8.0)))
            }
        }
    }
    var enabledButton = true
        set(value) {
            field = value
            updateState()
        }
    private var overButton = false
        set(value) {
            field = value
            updateState()
        }

    fun updateState() {
        when {
            !enabledButton -> alpha = 0.3
            overButton -> alpha = 1.0
            else -> alpha = 0.8
        }
    }

    init {
        //this += this.solidRect(bounds.width, bounds.height, Colors.TRANSPARENT_BLACK)
        this += g.apply {
            mouseEnabled = true
        }
        this += textField.position(8, 8)

        mouse {
            over { overButton = true }
            out { overButton = false }
        }
        onClick {
            if (enabledButton) handler()
        }
        updateState()
    }
}
