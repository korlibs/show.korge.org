package extension

import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import kotlinx.browser.*
import kotlinx.coroutines.*
import kotlinx.dom.*
import org.w3c.dom.*
import org.w3c.dom.events.*

actual val ext: Ext = object : Ext() {
	val canvasQuery by lazy { document.querySelector("#mycustomcanvas") }

	override val hasExternalLayout: Boolean get() = canvasQuery != null

	override fun preinit() {
		val dwindow = window.asDynamic()
		if (canvasQuery != null) {
			dwindow.korgwCanvasQuery = "#mycustomcanvas"
		}
	}

	override fun init(stage: Stage) {
		//window.onhashchange =
		//document.location!!.hash

		val registeredScenes = stage.registeredScenes

		val sceneTree = document.querySelector("#scene_tree")
		if (sceneTree != null) {
			sceneTree.textContent = ""

			for (entry in registeredScenes.values.groupBy { it.group }) {
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

		// Alternatively we could try CodeMirror
		val editor by lazy { if (window.asDynamic().ace) ace.edit("editor") else null }

		registerEvent("changedScene") { detail ->
			val className = detail.toString()
			val sceneId = "scene-${className}"

			val sceneInfo = registeredScenes[className]
			val editor = editor

			if (sceneInfo != null && editor != null) {
				try {
					editor.setTheme("ace/theme/monokai");
					editor.session.setMode("ace/mode/kotlin");
					editor.setReadOnly(true)

					launchImmediately(stage.coroutineContext) {
						val content =
							window.fetch("https://raw.githubusercontent.com/korlibs/show.korge.org/main/src/commonMain/kotlin/${sceneInfo.path}").await()
								.text().await()
						editor.setValue(content, 1)
						//editor.session.asDynamic().foldAll(2, 4)
						//console.warn(content)
					}
				} catch (e: dynamic) {
					console.error(e)
				}
			}
			for (active in document.querySelectorAll("a.active").toList()) {
				active.unsafeCast<HTMLElement>().removeClass("active")
			}
			document.querySelector("#$sceneId")?.addClass("active")
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

fun NodeList.toList(): List<Node> = (0 until length).map { this[it].unsafeCast<Node>() }