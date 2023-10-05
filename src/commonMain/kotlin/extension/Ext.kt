package extension

import korlibs.datastructure.*
import korlibs.inject.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.io.lang.*
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


abstract class ShowScene : Scene() {
}
