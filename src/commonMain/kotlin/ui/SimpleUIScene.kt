package ui

import com.soywiz.klock.*
import com.soywiz.korge.input.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korma.geom.*
import extension.*

class SimpleUIScene : ShowScene() {
	lateinit var font: Font
	lateinit var solidRect: SolidRect

	override suspend fun Container.sceneMain() {
		val container = this

		font = DefaultTtfFont.toBitmapFont(16.0)
		solidRect = solidRect(100, 100, Colors["#700ec7"]).position(200.0, 100.0).centered

		uiVerticalStack(width = 150.0) {
			uiButton(text = "Open Window List") {
				onClick {
					container.openLazyLongListWindow()
				}
			}
			uiButton(text = "Open Properties") {
				onClick {
					container.openPropertiesWindow()
				}
			}
		}.position(0.0, 32.0)

		//container.openLazyLongListWindow()

		uiWindow("Scrollable") {
			for (n in 0 until 10) {
				uiButton(text = "Hello $n").position(n * 32, n * 32)
			}
		}.centerOnStage()
	}

	fun Container.openLazyLongListWindow() {
		val window = uiWindow("Lazy long list") {
			uiSkin = UISkin {
				this.textFont = font
			}
			uiVerticalList(object : UIVerticalList.Provider {
				override val numItems: Int = 1000
				override val fixedHeight: Double = 20.0
				override fun getItemHeight(index: Int): Double = fixedHeight
				override fun getItemView(index: Int): View = UIText(" HELLO WORLD $index")
			})
		}.centerOnStage()

		tweenNoWait(
			window::x[0.0, window.x],
			window::y[0.0, window.y],
			window::width[0.0, window.width],
			window::height[0.0, window.height],
			window::alpha[0.3, 1.0],
			time = 0.3.seconds
		)
	}

	fun Container.openPropertiesWindow() {
		uiWindow("Properties", 400.0, 300.0) {
			it.container.mobileBehaviour = false
			it.container.overflowRate = 0.0
			uiVerticalStack(300.0) {
				uiText("Properties") { textColor = Colors.RED }
				//append(UIPropertyRow("Name")) { container.uiTextInput(solidRect.name ?: "") }
				uiPropertyNumberRow("Alpha", *UIEditableNumberPropsList(solidRect::alpha))
				uiPropertyNumberRow("Position", *UIEditableNumberPropsList(solidRect::x, solidRect::y, min = -1024.0, max = +1024.0, clamped = false))
				uiPropertyNumberRow("Size", *UIEditableNumberPropsList(solidRect::width, solidRect::height, min = -1024.0, max = +1024.0, clamped = false))
				uiPropertyNumberRow("Scale", *UIEditableNumberPropsList(solidRect::scaleX, solidRect::scaleY, min = -1.0, max = +1.0, clamped = false))
				uiPropertyNumberRow("Rotation", *UIEditableNumberPropsList(solidRect::rotationDeg, min = -360.0, max = +360.0, clamped = true))
				val skewProp = uiPropertyNumberRow("Skew", *UIEditableNumberPropsList(solidRect::skewXDeg, solidRect::skewYDeg, min = -360.0, max = +360.0, clamped = true))
				append(UIPropertyRow("Visible")) {
					this.container.append(uiCheckBox(checked = solidRect.visible, text = "").also {
						it.onChange {
							solidRect.visible = it.checked
						}
					})
				}

				//println(skewProp.getVisibleGlobalArea())

			}
		}.centerOnStage()
	}
}

private var View.rotationDeg: Double
	get() = rotation.degrees
	set(value) { rotation = value.degrees }

private var View.skewXDeg: Double
	get() = skewX.degrees
	set(value) { skewX = value.degrees }

private var View.skewYDeg: Double
	get() = skewY.degrees
	set(value) { skewY = value.degrees }
