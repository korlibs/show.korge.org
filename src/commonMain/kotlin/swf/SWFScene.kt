package swf

import extension.*
import korlibs.event.*
import korlibs.image.vector.*
import korlibs.io.async.*
import korlibs.io.file.*
import korlibs.io.file.std.*
import korlibs.korge.ext.swf.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.render.*

class SWFScene : AutoShowScene() {
    //val rastMethod = ShapeRasterizerMethod.X4 // Fails on native
    val rastMethod = ShapeRasterizerMethod.NONE
    //val rastMethod = ShapeRasterizerMethod.X1
    var graphicsRenderer = GraphicsRenderer.SYSTEM
    //val graphicsRenderer = GraphicsRenderer.GPU

    val config = SWFExportConfig(
        rasterizerMethod = rastMethod,
        generateTextures = false,
        //generateTextures = true,
        graphicsRenderer = graphicsRenderer,
    )

    override suspend fun SContainer.main() {
        //uiButton("HELLO")

        val extraSwfContainer = container {
            this += resourcesVfs["swf/morph.swf"].readSWF(views, config, false).createMainTimeLine()
            this += resourcesVfs["swf/dog.swf"].readSWF(views, config, false).createMainTimeLine()
            this += resourcesVfs["swf/test1.swf"].readSWF(views, config, false).createMainTimeLine().position(400, 0)
            this += resourcesVfs["swf/demo3.swf"].readSWF(views, config, false).createMainTimeLine()
        }

        fun loadSwf(files: List<VfsFile>) {
            if (files.isEmpty()) return
            launchImmediately {
                extraSwfContainer.removeChildren()
                for (file in files) {
                    val swf = file.readSWF(views, config, false)
                    swf.graphicsRenderer = graphicsRenderer
                    val timeline = swf.createMainTimeLine()
                    extraSwfContainer += timeline
                    val realBounds = Rectangle(0, 0, swf.width, swf.height).applyScaleMode(this@main.getLocalBounds(), ScaleMode.FIT, Anchor.CENTER)
                    //timeline.xy(realBounds.x, realBounds.y).scale(realBounds.width / swf.width, realBounds.height / swf.height)
                    //println("realBounds=$realBounds")
                    timeline.xy(realBounds.x, realBounds.y).sizeScaled(Size(realBounds.width, realBounds.height))
                    extraSwfContainer.uiHorizontalStack {
                        uiComboBox(items = timeline.stateNames).also {
                            it.onSelectionUpdate {
                                it.selectedItem?.let { timeline.play(it) }
                            }
                        }
                        uiComboBox(items = GraphicsRenderer.values().toList()).also {
                            it.selectedItem = graphicsRenderer
                            it.onSelectionUpdate {
                                it.selectedItem?.let { renderer ->
                                    graphicsRenderer = renderer
                                    swf.graphicsRenderer = renderer
                                }
                            }
                        }
                    }
                    //println("swf=${swf.width}x${swf.height}, timeline.getLocalBounds()=${timeline.getLocalBounds()}, this@demo.getLocalBounds()=${this@demo.getLocalBounds()}, realBounds=$realBounds")
                }
            }
        }

        //uiButton("Load or drag SWF...", size = UIButton.DEFAULT_SIZE.copy(width = UIButton.DEFAULT_SIZE.width * 2))
        this += UIButton(text = "Load or drag SWF...")
            .xy(510, 0)
            .clicked {
                launchImmediately {
                    val files = gameWindow.openFileDialog(filter = FileFilter("SWF files" to listOf("*.swf")), write = false, multi = false)
                    loadSwf(files)
                }
            }

        onDropFile {
            println("DropFileEvent: $it")
            if (it.type == DropFileEvent.Type.DROP) {
                val files = it.files ?: return@onDropFile
                loadSwf(files)
            }
        }
        //this += localVfs("/tmp/5.swf").readSWF(views, config, false).createMainTimeLine().scale(4.0)
    }
}
