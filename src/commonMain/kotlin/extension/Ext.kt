package extension

import korlibs.datastructure.*
import korlibs.image.color.*
import korlibs.inject.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.io.lang.*
import korlibs.math.geom.*
import korlibs.time.*
import kotlin.reflect.*

expect val ext: Ext

open class Ext {
	open val hasExternalLayout: Boolean get() = false

	open fun preinit() {
	}

	open fun init(stage: Stage) {
	}

	open fun registerEvent(event: String, handler: (detail: Any?) -> Unit) {
	}

	open fun dispatchCustomEvent(event: String, detail: Any?) {
	}

	open fun getSelectedSceneName(): String? {
		return null
	}
}

fun Stage.register(vararg sceneInfos: SceneInfo<*>) {
	for (sceneInfo in sceneInfos) {
		injector.mapPrototype(sceneInfo.clazz as KClass<ShowScene>) { sceneInfo.gen(this) }
		registeredScenes[sceneInfo.className] = sceneInfo
	}
}

class SceneInfo<T : ShowScene>(
	val title: String,
	val group: String,
	val path: String,
	val clazz: KClass<out T>,
	val gen: Injector.() -> T
) {
	val className get() = clazz.portableSimpleName
	override fun toString(): String = title
}

inline fun <reified T : ShowScene> SceneInfo(
	title: String = "title",
	group: String = "group",
	srcPath: String = "/src/commonMain/kotlin/main.kt",
	noinline gen: Injector.() -> T
): SceneInfo<T> {
	return SceneInfo(title, group, srcPath, T::class, gen)
}

val Stage.registeredScenes by extraProperty { LinkedHashMap<String, SceneInfo<out ShowScene>>() }

abstract class AutoShowScene : ShowScene() {
	final override suspend fun SContainer.sceneMain() {
		showSpinner()
		try {
			main()
		} finally {
			hideSpinner()
		}
	}

	abstract suspend fun SContainer.main()
}

abstract class ShowScene : Scene() {
	private var spinner: Graphics? = null

	fun SContainer.showSpinner() {
		hideSpinner()
		spinner = graphics {
			stroke(Colors.WHITE, lineWidth = 6.0) {
				arc(Point(0, 0), 48.0, 0.degrees, 60.degrees)
				arc(Point(0, 0), 48.0, 180.degrees, (180 + 60).degrees)
			}
		}.position(width * 0.5, height * 0.5)
		spinner!!.addUpdater { rotation += (it.seconds * 100).degrees }
	}

	fun hideSpinner() {
		if (spinner != null) {
			spinner?.removeFromParent()
			spinner = null
		}
	}
}
