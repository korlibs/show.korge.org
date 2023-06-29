package scene1

import korlibs.time.*
import korlibs.korge.tween.*
import korlibs.korge.view.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import extension.*

class Scene1 : ShowScene() {
	override suspend fun SContainer.sceneMain() {
		val minDegrees = (-16).degrees
		val maxDegrees = (+16).degrees

		val image = image(resourcesVfs["korge.png"].readBitmap()) {
			rotation = maxDegrees
			anchor(.5, .5)
			scale(.8)
			position(400, 300)
		}

		while (true) {
			image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
			image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		}
	}
}