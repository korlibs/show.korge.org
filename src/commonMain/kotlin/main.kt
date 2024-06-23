import anchorscale.*
import box2d.*
import bunnymark.*
import dungeon.DungeonScene
import korlibs.datastructure.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.io.async.*
import easings.*
import extension.*
import filters.*
import gestures.*
import k3d.K3DScene
import korlibs.image.vector.*
import korlibs.image.vector.format.*
import korlibs.korge.input.*
import korlibs.korge.view.filter.*
import korlibs.math.geom.*
import korlibs.platform.*
import korlibs.render.*
import scene1.*
import scene2.*
import spine.*
import swf.*
import ui.*

suspend fun main() {
	ext.preinit()
	Korge(
		title = "KorGE Web Samples",
		virtualSize = Size(800, 600),
		backgroundColor = Colors["#2b2b2b"],
		quality = GameWindow.Quality.PERFORMANCE
		//quality = GameWindow.Quality.QUALITY
	) {
		val GROUP_BASICS = "Basics"
		val GROUP_ADVANCED = "Advanced"
		val GROUP_PHYSICS = "Physics"
		val GROUP_SKELETON = "Skeleton"
		val GROUP_INPUT = "Input"
		val GROUP_PERFORMANCE = "Performance"
		val GROUP_UI = "UI"
		val GROUP_3D = "3D"
		val GROUP_SWF = "SWF"
		val GROUP_TILEMAP = "TileMaps"
		register(
			SceneInfo(title = "Rotating Image", group = GROUP_BASICS, srcPath = "scene1/Scene1.kt") { Scene1() },
			SceneInfo(title = "Tinting", group = GROUP_BASICS, srcPath = "scene2/Scene2.kt") { Scene2() },
			SceneInfo(title = "Easing", group = GROUP_BASICS, srcPath = "easings/EasingsScene.kt") { EasingsScene() },
			SceneInfo(title = "Anchor/Scale", group = GROUP_BASICS, srcPath = "anchorscale/MainUIImageTester.kt") { MainUIImageTester() },
			SceneInfo(title = "Filters", group = GROUP_ADVANCED, srcPath = "filters/FiltersScene.kt") { FiltersScene() },
			SceneInfo(title = "3D", group = GROUP_3D, srcPath = "k3d/K3DScene.kt") { K3DScene() },
			SceneInfo(title = "SWF", group = GROUP_SWF, srcPath = "swf/SWFScene.kt") { SWFScene() },
			SceneInfo(title = "Simple Box2d", group = GROUP_PHYSICS, srcPath = "box2d/SimpleBox2dScene.kt") { SimpleBox2dScene() },
			//SceneInfo(title = "Dragonbones", group = GROUP_SKELETON, srcPath = "dragonbones/DragonbonesScene.kt") { DragonbonesScene() }, // JS-IR has issues with this demo
			SceneInfo(title = "Spine", group = GROUP_SKELETON, srcPath = "spine/SpineScene.kt") { SpineScene() },
			SceneInfo(title = "Gestures", group = GROUP_INPUT, srcPath = "gestures/GesturesScene.kt") { GesturesScene() },
			SceneInfo(title = "Bunnymark", group = GROUP_PERFORMANCE, srcPath = "bunnymark/BunnymarkScene.kt") { BunnymarkScene() },
			SceneInfo(title = "UI", group = GROUP_UI, srcPath = "ui/SimpleUIScene.kt") { SimpleUIScene() },
			SceneInfo(title = "Dungeon Explorer", group = GROUP_TILEMAP, srcPath = "dungeon/DungeonScene.kt") { DungeonScene() },
			SceneInfo(title = "Snake", group = GROUP_TILEMAP, srcPath = "snake/scene/IngameScene.kt") { snake.scene.SnakeScene() },
		)

		// Elements
		run {
			this.mainSceneContainer = sceneContainer()
			//ext.hasExternalLayout
			if (!Platform.isJs) {
				uiComboBox(items = registeredScenes.values.toList()).onSelectionUpdate {
					launchImmediately {
						changeToScene(stage, it.selectedItem?.className)
					}
				}
			} else {
				// On JS we have
			}
		}

		ext.init(this)
		ext.registerEvent("changeScene") { detail ->
			launchImmediately {
				changeToScene(stage, detail.toString())
			}
		}

		ext.registerEvent("hashchange") { detail ->
			launchImmediately {
				changeToSceneDefault(stage)
			}
		}

		changeToSceneDefault(stage)

		addFullScreenButton()

	}
}

var Stage.mainSceneContainer: SceneContainer? by extraProperty { null }

private fun Container.addFullScreenButton() {
	vectorImage(SVG(
		// language=svg
		"""
			<?xml version="1.0" encoding="iso-8859-1"?>
			<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
			<svg fill="#000000" height="800px" width="800px" version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" 
				 viewBox="0 0 512 512" xml:space="preserve">
			<g style='fill:white'>
				<path d="M192,64H32C14.328,64,0,78.328,0,96v96c0,17.672,14.328,32,32,32s32-14.328,32-32v-64h128c17.672,0,32-14.328,32-32 S209.672,64,192,64z"/>
				<path d="M480,64H320c-17.672,0-32,14.328-32,32s14.328,32,32,32h128v64c0,17.672,14.328,32,32,32s32-14.328,32-32V96 C512,78.328,497.672,64,480,64z"/>
				<path d="M480,288c-17.672,0-32,14.328-32,32v64H320c-17.672,0-32,14.328-32,32s14.328,32,32,32h160c17.672,0,32-14.328,32-32v-96 C512,302.328,497.672,288,480,288z"/>
				<path d="M192,384H64v-64c0-17.672-14.328-32-32-32S0,302.328,0,320v96c0,17.672,14.328,32,32,32h160c17.672,0,32-14.328,32-32 S209.672,384,192,384z"/>
			</g>
			</svg>
		""".trimIndent()
	).scaled(0.1)) {
		filters(DropshadowFilter())
		anchor(Anchor.BOTTOM_RIGHT)
		xy(800, 600)
		cursor = Cursor.HAND
		mouse {
			onClick {
				views.gameWindow.toggleFullScreen()
			}
		}
	}
}

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
