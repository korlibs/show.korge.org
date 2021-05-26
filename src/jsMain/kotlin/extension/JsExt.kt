package extension

import com.soywiz.korge.view.*
import kotlinx.browser.*
import org.w3c.dom.*
import org.w3c.dom.events.*

actual val ext: Ext = object : Ext() {
	override fun init(stage: Stage) {
		document.location!!.hash
	}

	override fun registerEvent(event: String, handler: (detail: Any?) -> Unit) {
		window.addEventListener(event, { ev: Event ->
			handler((ev as CustomEvent).detail)
		})
	}

	override fun dispatchCustomEvent(event: String, detail: Any?) {
		window.dispatchEvent(CustomEvent(type = event, CustomEventInit(detail)))
	}

	override fun getSelectedSceneName(): String? {
		return document.location?.hash?.trim('#')
	}
}
