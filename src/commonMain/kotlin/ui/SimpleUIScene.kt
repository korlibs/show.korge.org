package ui

import korlibs.time.*
import korlibs.korge.input.*
import korlibs.korge.tween.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.math.geom.*
import extension.*
import korlibs.korge.style.styles
import korlibs.korge.style.textColor
import korlibs.korge.style.textFont
import korlibs.korge.view.align.centerOnStage

class SimpleUIScene : ShowScene() {
	lateinit var font: Font
	lateinit var solidRect: SolidRect

	override suspend fun SContainer.sceneMain() {
		val container = this

		font = DefaultTtfFont.toBitmapFont(16f)
		solidRect = solidRect(100, 100, Colors["#700ec7"]).position(200.0, 100.0).centered

		uiVerticalStack(width = 150f) {
			uiButton(label = "Open Window List") {
				onClick {
					container.openLazyLongListWindow()
				}
			}
			uiButton(label = "Open Properties") {
				onClick {
					container.openPropertiesWindow()
				}
			}
		}.position(0.0, 32.0)

		//container.openLazyLongListWindow()

		uiWindow("Scrollable") {
			for (n in 0 until 10) {
				uiButton(label = "Hello $n").position(n * 32, n * 32)
			}
		}.centerOnStage()
	}

	fun Container.openLazyLongListWindow() {
		val window = uiWindow("Lazy long list") {
			styles.textFont = font
			uiVerticalList(object : UIVerticalList.Provider {
				override val numItems: Int = 1000
				override val fixedHeight: Float = 20f
				override fun getItemHeight(index: Int): Float = fixedHeight
				override fun getItemView(index: Int, vlist: UIVerticalList): View = UIText(" HELLO WORLD $index")
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
		uiWindow("Properties", Size(400, 300)) {
			it.container.mobileBehaviour = false
			it.container.overflowRate = 0f
			uiVerticalStack(300f) {
				uiText("Properties").also {
					it.styles.textColor = Colors.RED
				}
				//append(UIPropertyRow("Name")) { container.uiTextInput(solidRect.name ?: "") }
				uiPropertyNumberRow("Alpha", *UIEditableNumberPropsList(solidRect::alpha))
				uiPropertyNumberRow("Position", *UIEditableNumberPropsList(solidRect::x, solidRect::y, min = -1024f, max = +1024f, clamped = false))
				uiPropertyNumberRow("Size", *UIEditableNumberPropsList(solidRect::width, solidRect::height, min = -1024f, max = +1024f, clamped = false))
				uiPropertyNumberRow("Scale", *UIEditableNumberPropsList(solidRect::scaleX, solidRect::scaleY, min = -1f, max = +1f, clamped = false))
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
	get() = rotation.degrees.toDouble()
	set(value) { rotation = value.degrees }

private var View.skewXDeg: Double
	get() = skewX.degrees.toDouble()
	set(value) { skewX = value.degrees }

private var View.skewYDeg: Double
	get() = skewY.degrees.toDouble()
	set(value) { skewY = value.degrees }
