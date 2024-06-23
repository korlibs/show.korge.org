package korlibs.korge.ext.swf

import korlibs.korge.animate.*
import korlibs.korge.animate.serialization.AniFile
import korlibs.korge.animate.serialization.writeTo
import korlibs.korge.view.*
import korlibs.io.file.VfsFile
import kotlin.coroutines.coroutineContext

/*
open class SwfResourceProcessor : ResourceProcessor("swf") {
	companion object : SwfResourceProcessor()

	override val version: Int = AniFile.VERSION
	override val outputExtension: String = "ani"

	override suspend fun processInternal(inputFile: VfsFile, outputFile: VfsFile) {
        viewsLogSuspend { viewsLog ->
            val lib = inputFile.readSWF(AnLibrary.Context(viewsLog.views))
            val config = lib.swfExportConfig
            lib.writeTo(outputFile, config.toAnLibrarySerializerConfig(compression = 1.0))
        }
	}
}
*/
