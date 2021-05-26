import com.soywiz.kds.*
import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import extension.*
import scene1.*
import scene2.*

suspend fun main() = Korge(width = 800, height = 600, bgcolor = Colors["#2b2b2b"]) {
	register(
		SceneInfo(title = "My Scene 2", group = "My Group", srcPath = "src/commonMain/kotlin/scene2/Scene2.kt") { Scene2() },
		SceneInfo(title = "My Scene 1", group = "My Group", srcPath = "src/commonMain/kotlin/scene1/Scene1.kt") { Scene1() },
	)

	this.mainSceneContainer = sceneContainer()

	ext.init(this)
	ext.registerEvent("changeScene") { detail ->
		launchImmediately {
			changeToScene(stage, detail.toString())
		}
	}
	ext.dispatchCustomEvent("scenes", registeredScenes.map {
		val value = it.value
		arrayOf(value.className, value.title, value.group, value.path)
	}.toTypedArray())

	ext.registerEvent("hashchange") { detail ->
		launchImmediately {
			changeToSceneDefault(stage)
		}
	}

	changeToSceneDefault(stage)
}

var Stage.mainSceneContainer: SceneContainer? by extraProperty { null }

suspend fun changeToSceneDefault(stage: Stage) {
	changeToScene(stage, ext.getSelectedSceneName())
}

suspend fun changeToScene(stage: Stage, sceneName: String?) {
	val registeredScenes = stage.registeredScenes
	val realSceneName = sceneName ?: registeredScenes.keys.first()
	val sceneInfo = registeredScenes[realSceneName] ?: registeredScenes.values.first()
	stage.mainSceneContainer!!.changeTo(sceneInfo.clazz)
	ext.dispatchCustomEvent("changedScene", sceneInfo.className)
}
