package gestures

import korlibs.time.*
import korlibs.korge.input.*
import korlibs.korge.tween.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import extension.*

class GesturesScene : ShowScene() {
    override suspend fun SContainer.sceneMain() {
        val minDegrees = (-16).degrees
        val maxDegrees = (+16).degrees
        lateinit var image: Image

        val container = container {
            position(256, 256)
            image = image(resourcesVfs["korge.png"].readBitmap()) {
                rotation = maxDegrees
                anchor(.5, .5)
                scale(.8)
            }
        }

        text("Zoom and rotate with two fingers")

        touch {
            var startImageRatio = 1.0
            var startRotation = 0.degrees

            scaleRecognizer(start = {
                startImageRatio = image.scale
            }) {
                image.scale = startImageRatio * this.ratio
            }

            rotationRecognizer(start = {
                startRotation = container.rotation
            }) {
                container.rotation = startRotation + this.delta
            }
        }

        image.mouse {
            click {
                image.alpha = if (image.alpha > 0.5) 0.5 else 1.0
            }
        }

        addFixedUpdater(2.timesPerSecond) {
            println(views.input.activeTouches)
        }

        uiButton(label = "1") {
            position(10, 380)
            onPress { println("TAPPED ON 1") }
        }
        uiButton(label = "2") {
            position(150, 380)
            onPress { println("TAPPED ON 2") }
        }

        uiButton(label = "3") {
            position(300, 380)
            onPress { println("TAPPED ON 3") }
        }

        while (true) {
            image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
            image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
        }
    }
}