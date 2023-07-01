package k3d

import extension.ShowScene
import korlibs.event.DropFileEvent
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.io.async.launchImmediately
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.*
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiSlider
import korlibs.korge.view.*
import korlibs.korge3d.*
import korlibs.korge3d.format.gltf2.GLTF2View
import korlibs.korge3d.format.gltf2.gltf2View
import korlibs.korge3d.format.gltf2.readGLTF2
import korlibs.korge3d.shape.axisLines
import korlibs.korge3d.shape.polyline3D
import korlibs.math.geom.*
import korlibs.render.FileFilter
import korlibs.render.openFileDialog
import korlibs.time.measureTimeWithResult
import korlibs.time.milliseconds

class K3DScene : ShowScene() {
    lateinit var dropFileRect: SolidRect

    override suspend fun SContainer.sceneMain() {
        var rotationY = 0.degrees
        var rotationX = Angle.QUARTER
        var cameraDistance = 10f
        var centerPoint = Vector3.ZERO

        scene3D {
            camera = Camera3D.Perspective()
            axisLines(length = 4f)

            val centerPointAxisLines = axisLines2(length = .2f)

            val quat = Quaternion.IDENTITY

            //val slider = uiSlider(1f, min = -1f, max = 2f, step = .0125f)
            //    .also { slider -> slider.onChange { stage3D!!.occlusionStrength = slider.value.toFloat() } }
            //    .xy(30, 30)
            //    .scale(1)

            val slider2 = uiSlider(1f, min = 0f, max = 1f, step = .0125f)
                .also { slider -> slider.onChange { stage3D!!.occlusionStrength = slider.value.toFloat() } }
                .xy(200, 30)
                .scale(1)

            val koral = resourcesVfs["k3d/Koral.glb"].readGLTF2()
            val walking0Skin =
                GLTF2View(resourcesVfs["k3d/Walking0.glb"].readGLTF2(), autoAnimate = false).viewSkins.first()
            val walking1Skin =
                GLTF2View(resourcesVfs["k3d/Walking1.glb"].readGLTF2(), autoAnimate = false).viewSkins.first()
            val slowRunSkin = GLTF2View(resourcesVfs["k3d/SlowRun.glb"].readGLTF2(), autoAnimate = false).viewSkins.first()
            val fastRunSkin = GLTF2View(resourcesVfs["k3d/FastRun.glb"].readGLTF2(), autoAnimate = false).viewSkins.first()
            val hipHopDancingSkin =
                GLTF2View(resourcesVfs["k3d/HipHopDancing.glb"].readGLTF2(), autoAnimate = false).viewSkins.first()
            var koralView = gltf2View(koral, autoAnimate = false).position(-1, 0, 0)

            addUpdater {
                walking0Skin.view.updateAnimationDelta(it)
                walking1Skin.view.updateAnimationDelta(it)
                slowRunSkin.view.updateAnimationDelta(it)
                fastRunSkin.view.updateAnimationDelta(it)
                hipHopDancingSkin.view.updateAnimationDelta(it)
                koralView.viewSkins.first().writeFrom(walking0Skin, hipHopDancingSkin, slider2.value.toFloat())
            }

            gltf2View(resourcesVfs["k3d/Gest.glb"].readGLTF2()).position(1, 0, 0)


            suspend fun loadFile(file: VfsFile) {
                val (it, time) = measureTimeWithResult {
                    file.readGLTF2()
                }
                println("Loaded GLTF2 in $time...")
                koralView.removeFromParent()
                koralView = GLTF2View(it).addTo(this)
                //updateEstimatedViewScale()
            }

            uiButton("Load...") { onClick {
                gameWindow.openFileDialog(FileFilter("GLTF2" to listOf("*.glb", "*.gltf", "*.gltf2")))?.firstOrNull()?.let {
                    loadFile(it)
                }
            } }

            onEvents(*DropFileEvent.Type.ALL) {
                when (it.type) {
                    DropFileEvent.Type.START -> {
                        dropFileRect.visible = true
                    }
                    DropFileEvent.Type.END -> dropFileRect.visible = false
                    DropFileEvent.Type.DROP -> {
                        launchImmediately {
                            it.files?.firstOrNull()?.let { loadFile(it) }
                        }
                    }
                }            }

            camera = koralView.gltf.cameras.firstOrNull()?.perspective?.toCamera() ?: Camera3D.Perspective()

            fun updateOrbitCamera() {
                centerPointAxisLines.position = centerPoint
                camera.orbitAround(centerPoint, cameraDistance, rotationY, rotationX)
            }

            onMagnify {
                //camera.position.setTo(0f, 1f, camera.position.z + it.amount)
                //camera.position += Vector4.ZERO.copy(z = -it.amount * 2)
                cameraDistance -= it.amount * 2
                updateOrbitCamera()
            }
            onScroll {
                //println("onscroll: ${it.scrollDeltaXPixels}, ${it.scrollDeltaYPixels}")
                //zoom -= (it.scrollDeltaYPixels / 240)
                //updateZoom()
                centerPoint += Vector3(
                    it.scrollDeltaXPixels * 0.25f,
                    -it.scrollDeltaYPixels * 0.25f,
                    0f,
                )
                updateOrbitCamera()
            }

            //koralView.rotation(quat)
            fun rotate(deltaY: Angle, deltaX: Angle) {
                //koralView.rotation(quat * Quaternion.fromAxisAngle(Vector3.UP, rotationY) * Quaternion.fromAxisAngle(Vector3.RIGHT, rotationX))
                rotationY += deltaY
                rotationX = adjustOrbitElevation(rotationX + deltaX)
                //camera.transform.setTranslationAndLookAt()
                updateOrbitCamera()
            }

            updateOrbitCamera()

            keys {
                downFrame(Key.LEFT, 4.milliseconds) { rotate(-1.degrees, 0.degrees) }
                downFrame(Key.RIGHT, 4.milliseconds) { rotate(+1.degrees, 0.degrees) }
                downFrame(Key.UP, 4.milliseconds) { rotate(0.degrees, +1.degrees) }
                downFrame(Key.DOWN, 4.milliseconds) { rotate(0.degrees, -1.degrees) }
            }

            solidRect(2000, 1000, Colors.TRANSPARENT).xy(0, 100).onMouseDrag {
                rotate(it.deltaDx.degrees, it.deltaDy.degrees)
            }
        }
    }
}

fun <T : View3D> T.setTranslation(p: Vector3, t: Vector3): T {
    transform.setTranslationAndLookAt(p.x, p.y, p.z, t.x, t.y, t.z)
    invalidateRender()
    return this
}

fun <T : View3D> T.positionLookingAt(p: Vector3, t: Vector3): T {
    transform.setTranslationAndLookAt(p.x, p.y, p.z, t.x, t.y, t.z)
    invalidateRender()
    return this
}

fun Angle.clamp(min: Angle, max: Angle): Angle = min(max(this, min), max)

fun adjustOrbitElevation(angle: Angle): Angle {
    return angle.clamp(Angle.ZERO + 0.01.degrees, Angle.HALF - 0.01.degrees)
}

fun <T : View3D> T.orbitAround(t: Vector3, distance: Float, azimuth: Angle, elevation: Angle): T {
    val r = distance

    // @TODO: Angle clamp
    val theta = azimuth
    val phi = adjustOrbitElevation(elevation)
    val sinPhi = sin(phi)
    val p = Vector3(
        r * sinPhi * sin(theta), // x
        r * cos(phi),            // y
        r * sinPhi * cos(theta)  // z
    )

    //val cosElevation = cos(elevation)
    //val p = Vector3(
    //    r * cosElevation * cos(azimuth), // x
    //    r * cosElevation * sin(azimuth), // y
    //    r * sin(elevation)               // z
    //)
    //this.transform.setTranslation(p.x, p.y, p.z)
    return positionLookingAt(t + p, t)
    //return this
}

fun Container3D.axisLines2(basePosition: Vector3 = Vector3.ZERO, length: Float = 10f, lengthWhiteScale: Float = .25f): Container3D {
    val ll = length
    val l2 = length * lengthWhiteScale
    return container3D {
        position(basePosition)
        polyline3D(Colors["#e20050"]) {
            moveTo(Vector3(-ll, 0f, 0f))
            lineTo(Vector3(ll, 0f, 0f))
        }
        polyline3D(Colors.MEDIUMVIOLETRED) {
            moveTo(Vector3.DOWN * ll)
            lineTo(Vector3.UP * ll)
        }
        polyline3D(Colors["#8cb04d"]) {
            moveTo(Vector3(0f, 0f, -ll))
            lineTo(Vector3(0f, 0f, ll))
        }
        polyline3D(Colors.WHITE) {
            moveTo(Vector3(0f, 0f, 0f))
            lineTo(Vector3(l2, 0f, 0f))
            moveTo(Vector3(0f, 0f, 0f))
            lineTo(Vector3(0f, l2, 0f))
            moveTo(Vector3(0f, 0f, 0f))
            lineTo(Vector3(0f, 0f, l2))
        }
    }
}
