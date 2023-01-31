package easings

import com.soywiz.korge.input.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.vector.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.interpolation.*
import extension.*
import kotlinx.coroutines.*

class EasingsScene : ShowScene() {
    override suspend fun Container.sceneMain() {
        var ballTween: Job? = null
        val ball = circle(64.0, Colors.PURPLE).xy(64, 64)

        fun renderEasing(easing: Easing): View {
            return Container().apply {
                val bg = solidRect(64, -64, Colors.BLACK.withAd(0.2))
                //graphics(renderer = GraphicsRenderer.SYSTEM) { shape ->
                graphics {
                    stroke(Colors.RED, StrokeInfo(thickness = 4.0)) {
                        this.line(0.0, 0.0, 0.0, -64.0)
                        this.line(0.0, 0.0, 64.0, 0.0)
                    }
                    stroke(Colors.WHITE, StrokeInfo(thickness = 2.0)) {
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
                text("$easing", textSize = textSize).xy(0.0, textSize)
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
            //Easing.cubic(.86, .13, .22, .84),
        )

        var mn = 0
        for (my in 0 until 4) {
            for (mx in 0 until 8) {
                val easing = easings.getOrNull(mn++) ?: continue
                renderEasing(easing).xy(50 + mx * 100, 300 + my * 100).addTo(this)
            }
        }
    }
}