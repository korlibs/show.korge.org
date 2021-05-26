package extension

import com.soywiz.korge.view.*
import kotlinx.browser.*
import org.w3c.dom.*
import org.w3c.dom.events.*

actual val ext: Ext = object : Ext() {
	val canvasQuery by lazy { document.querySelector("#mycustomcanvas") }
	override fun preinit() {
		val dwindow = window.asDynamic()
		if (canvasQuery != null) {
			dwindow.korgwCanvasQuery = "#mycustomcanvas"
		}
	}

	override fun init(stage: Stage) {
		//window.onhashchange =
		//document.location!!.hash

		val sceneTree = document.querySelector("#scene_tree")
		if (sceneTree != null) {
			sceneTree.textContent = ""
			for (entry in stage.registeredScenes.values.groupBy { it.group }) {
				val group = entry.key
				val groupDiv = document.createElement("h2")
				val groupDivTree = document.createElement("div")
				groupDiv.textContent = group
				sceneTree.appendChild(groupDiv)
				sceneTree.appendChild(groupDivTree)
				for (scene in entry.value) {
					val className = scene.className
					val title = scene.title
					val path = scene.path
					val elementNode = document.createElement("a").unsafeCast<HTMLAnchorElement>()
					elementNode.id = "scene-${className}"
					elementNode.textContent = title
					elementNode.href = "#${className}"
					groupDivTree.appendChild(elementNode)
				}
			}
		}
	}

	override fun registerEvent(event: String, handler: (detail: Any?) -> Unit) {
		window.addEventListener(event, { ev: Event ->
			handler(ev.unsafeCast<CustomEvent>().detail)
		})
	}

	override fun dispatchCustomEvent(event: String, detail: Any?) {
		window.dispatchEvent(CustomEvent(type = event, CustomEventInit(detail)))
	}

	override fun getSelectedSceneName(): String? {
		return document.location?.hash?.trim('#')
	}
}
