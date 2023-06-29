package box2d

import korlibs.korge.box2d.*
import korlibs.korge.input.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.math.geom.*
import korlibs.math.random.*
import extension.*
import korlibs.math.interpolation.interpolate
import korlibs.math.interpolation.toRatio
import org.jbox2d.dynamics.*
import kotlin.random.*

class SimpleBox2dScene : ShowScene() {
	val random = Random(0L)

	override suspend fun SContainer.sceneMain() {
		val stage = stage!!
		fixedSizeContainer(Size(stage.width, stage.height)) {
			solidRect(50, 50, Colors.RED).position(400, 50).rotation(30.degrees)
				.registerBodyWithFixture(type = BodyType.DYNAMIC, density = 2, friction = 0.01)
			solidRect(50, 50, Colors.RED).position(300, 100).registerBodyWithFixture(type = BodyType.DYNAMIC)
			solidRect(50, 50, Colors.RED).position(450, 100).rotation(15.degrees)
				.registerBodyWithFixture(type = BodyType.DYNAMIC)
			solidRect(600, 100, Colors.WHITE).position(100, 500).registerBodyWithFixture(
				type = BodyType.STATIC,
				friction = 0.2
			)

			onClick {
				val pos = it.currentPosLocal
				solidRect(50, 50, Colors.RED)
					.position(pos.x, pos.y)
					.rotation(random[0.degrees, 90.degrees])
					.also { it.colorMul = random[Colors.RED, Colors.PURPLE] }
					.registerBodyWithFixture(type = BodyType.DYNAMIC)
			}

			uiButton(label = "Reset").position(stage.width - 128.0, 0.0).onClick { sceneContainer.changeTo(this@SimpleBox2dScene::class) }
		}
	}

	// @TODO: Will be available on the next version of korma
	private fun Random.nextDoubleInclusive(): Double = (this.nextInt(0x1000001).toDouble() / 0x1000000.toDouble())
	private operator fun Random.get(l: Angle, r: Angle): Angle = this.nextDoubleInclusive().toRatio().interpolateAngle(l, r)
}
