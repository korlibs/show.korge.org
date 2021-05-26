package box2d

import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import extension.*
import org.jbox2d.dynamics.*

class SimpleBox2dScene : ShowScene() {
	override suspend fun Container.sceneMain() {
		container {
			position(300, 300)
			solidRect(20, 20, Colors.RED).position(100, 100).centered.rotation(30.degrees).registerBodyWithFixture(type = BodyType.DYNAMIC, density = 2, friction = 0.01)
			solidRect(20, 20, Colors.RED).position(109, 75).centered.registerBodyWithFixture(type = BodyType.DYNAMIC)
			solidRect(20, 20, Colors.RED).position(93, 50).centered.rotation((-15).degrees).registerBodyWithFixture(type = BodyType.DYNAMIC)
			solidRect(400, 100, Colors.WHITE).position(100, 300).centered.registerBodyWithFixture(type = BodyType.STATIC, friction = 0.2)
		}
	}
}