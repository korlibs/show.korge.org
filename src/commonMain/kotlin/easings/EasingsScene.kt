package easings

import korlibs.korge.input.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.vector.*
import korlibs.math.geom.vector.*
import korlibs.math.interpolation.*
import extension.*
import korlibs.math.geom.Point
import kotlinx.coroutines.*

class EasingsScene : ShowScene() {
    override suspend fun SContainer.sceneMain() {
        var ballTween: Job? = null
        val ball = circle(64f, Colors.PURPLE).xy(64, 64)

        fun renderEasing(easing: Easing): View {
            return Container().apply {
                val bg = solidRect(64, -64, Colors.BLACK.withAd(0.2))
                //graphics(renderer = GraphicsRenderer.SYSTEM) { shape ->
                graphics {
                    stroke(Colors.RED, StrokeInfo(thickness = 4f)) {
                        this.line(Point(0, 0), Point(0.0, -64.0))
                        this.line(Point(0, 0), Point(64.0, 0.0))
                    }
                    stroke(Colors.WHITE, StrokeInfo(thickness = 2f)) {
                        var first = true
                        //val overflow = 8
                        val overflow = 0
                        for (n in (-overflow)..(64 + overflow)) {
                            val ratio = n.toDouble() / 64.0
                            val x = n.toDouble()
                            val y = easing(ratio) * 64
                            //println("x=$x, y=$y, ratio=$ratio")
                            if (first) {
                                first = false
                                moveTo(x, -y)
                            } else {
                                lineTo(x, -y)
                            }
                        }
                    }
                }.addTo(this)
                val textSize = 10.0
                text("$easing", textSize = textSize.toFloat()).xy(0.0, textSize)
                onOver { bg.color = Colors.BLACK.withAd(1.0) }
                onOut { bg.color = Colors.BLACK.withAd(0.2) }
                onClick {
                    ballTween?.cancel()
                    ballTween = ball.tweenAsync(ball::x[64.0, 64.0 + 512.0], easing = easing)
                }
            }
        }

        val easings = listOf(
            *Easing.ALL.values.toTypedArray(),
            Easing.cubic(.86, .13, .22, .84),
        )

        container {
            scale = 0.9
            var mn = 0
            for (my in 0 until 4) {
                for (mx in 0 until 8) {
                    val easing = easings.getOrNull(mn++) ?: continue
                    renderEasing(easing).xy(50 + mx * 100, 300 + my * 100).addTo(this)
                }
            }
        }
    }
}