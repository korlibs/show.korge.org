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
import korlibs.math.geom.Size
import korlibs.platform.*
import korlibs.render.GameWindow
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
		val GROUP_TILEMAP = "TileMaps"
		register(
			SceneInfo(title = "Rotating Image", group = GROUP_BASICS, srcPath = "scene1/Scene1.kt") { Scene1() },
			SceneInfo(title = "Tinting", group = GROUP_BASICS, srcPath = "scene2/Scene2.kt") { Scene2() },
			SceneInfo(title = "Easing", group = GROUP_BASICS, srcPath = "easings/EasingsScene.kt") { EasingsScene() },
			SceneInfo(title = "Anchor/Scale", group = GROUP_BASICS, srcPath = "anchorscale/MainUIImageTester.kt") { MainUIImageTester() },
			SceneInfo(title = "Filters", group = GROUP_ADVANCED, srcPath = "filters/FiltersScene.kt") { FiltersScene() },
			SceneInfo(title = "3D", group = GROUP_3D, srcPath = "k3d/K3DScene.kt") { K3DScene() },
			SceneInfo(title = "SWF", group = GROUP_3D, srcPath = "swf/SWFScene.kt") { SWFScene() },
			SceneInfo(title = "Simple Box2d", group = GROUP_PHYSICS, srcPath = "box2d/SimpleBox2dScene.kt") { SimpleBox2dScene() },
			//SceneInfo(title = "Dragonbones", group = GROUP_SKELETON, srcPath = "dragonbones/DragonbonesScene.kt") { DragonbonesScene() }, // JS-IR has issues with this demo
			SceneInfo(title = "Spine", group = GROUP_SKELETON, srcPath = "spine/SpineScene.kt") { SpineScene() },
			SceneInfo(title = "Gestures", group = GROUP_INPUT, srcPath = "gestures/GesturesScene.kt") { GesturesScene() },
			SceneInfo(title = "Bunnymark", group = GROUP_PERFORMANCE, srcPath = "bunnymark/BunnymarkScene.kt") { BunnymarkScene() },
			SceneInfo(title = "UI", group = GROUP_UI, srcPath = "ui/SimpleUIScene.kt") { SimpleUIScene() },
			SceneInfo(title = "Dungeon Explorer", group = GROUP_TILEMAP, srcPath = "dungeon/DungeonScene.kt") { DungeonScene() },
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
	}
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
