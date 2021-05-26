package scene1

import com.soywiz.klock.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import extension.*

class Scene1 : ShowScene() {
	override suspend fun Container.sceneMain() {
		val minDegrees = (-16).degrees
		val maxDegrees = (+16).degrees

		val a: UInt = 1u

		println(a)

		val image = image(resourcesVfs["korge.png"].readBitmap()) {
			rotation = maxDegrees
			anchor(.5, .5)
			scale(.8)
			position(256, 256)
		}

		while (true) {
			image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
			image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		}
	}
}