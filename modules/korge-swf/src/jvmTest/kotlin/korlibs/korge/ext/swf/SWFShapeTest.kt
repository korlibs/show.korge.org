package korlibs.korge.ext.swf

import korlibs.image.bitmap.*
import korlibs.korge.animate.*
import korlibs.korge.tests.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.vector.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import kotlin.test.*

class SWFShapeTest : ViewsForTesting() {
    @Test
    fun test() = suspendTest {
        //val swf = resourcesVfs["swf/main.swf"].readSWF(views, defaultConfig = SWFExportConfig(rasterizerMethod = ShapeRasterizerMethod.NONE))
        //val swf = resourcesVfs["swf/main.swf"].readSWF(views, defaultConfig = SWFExportConfig(rasterizerMethod = ShapeRasterizerMethod.X1))
        for (method in listOf(ShapeRasterizerMethod.NONE, ShapeRasterizerMethod.X1, ShapeRasterizerMethod.X4)) {
            val swf = resourcesVfs["swf/main.swf"].readSWF(
                AnLibrary.Context(views),
                defaultConfig = SWFExportConfig(rasterizerMethod = method, generateTextures = true)
            )
            val img = (swf.symbolsById[10] as AnSymbolShape).textureWithBitmap!!.bitmapSlice.extract().toBMP32()
            assertEquals(Colors.BLUE, img[58, 10])
            assertEquals(Colors.TRANSPARENT, img[58, 20])
        }
    }
}
