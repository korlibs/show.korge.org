import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import extension.*
import scene1.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
	register(
		SceneInfo(title = "My Scene", group = "My Group", srcPath = "src/commonMain/kotlin/scene1/Scene1.kt") { Scene1() },
	)

	val sceneContainer = sceneContainer()

	suspend fun changeToScene(sceneName: String?) {
		val registeredScenes = registeredScenes
		val realSceneName = sceneName ?: registeredScenes.keys.first()
		val sceneInfo = registeredScenes[realSceneName] ?: registeredScenes.values.first()
		sceneContainer.changeTo(sceneInfo.clazz)
		ext.dispatchCustomEvent("changedScene", sceneInfo.className)
	}

	ext.init(this)
	ext.registerEvent("changeScene") {
		launchImmediately {
			changeToScene(it.toString())
		}
	}
	ext.dispatchCustomEvent("scenes", registeredScenes.map {
		val value = it.value
		arrayOf(value.className, value.title, value.group, value.path)
	}.toTypedArray())
	changeToScene(ext.getSelectedSceneName())
}
