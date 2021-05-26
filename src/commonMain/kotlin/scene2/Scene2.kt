package scene2

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import extension.*

class Scene2 : ShowScene() {
	override suspend fun Container.sceneMain() {
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