package scene2

import korlibs.korge.input.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.math.geom.*
import extension.*

class Scene2 : ShowScene() {
	override suspend fun SContainer.sceneMain() {
		image(resourcesVfs["korge.png"].readBitmap()) {
			colorMul = Colors.RED
			rotation = 0.degrees
			anchor(.5, .5)
			scale(.8)
			position(400, 300)
			alpha = 0.5
			mouse {
				onOver { alpha = 1.0 }
				onOut { alpha = 0.5 }
			}
		}
	}
}