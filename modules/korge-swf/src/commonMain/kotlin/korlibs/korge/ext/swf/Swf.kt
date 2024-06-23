package korlibs.korge.ext.swf

import korlibs.datastructure.Extra
import korlibs.korfl.as3swf.*
import korlibs.korge.animate.*
import korlibs.korge.animate.serialization.*
import korlibs.korge.view.*
import korlibs.image.bitmap.*
import korlibs.image.vector.*
import korlibs.io.dynamic.*
import korlibs.io.file.*
import korlibs.io.serialization.yaml.*
import kotlin.coroutines.*
import kotlin.native.concurrent.ThreadLocal

data class SWFExportConfig(
    val debug: Boolean = false,
    val mipmaps: Boolean = true,
    val antialiasing: Boolean = true,
    val rasterizerMethod: ShapeRasterizerMethod = ShapeRasterizerMethod.X4,
    val exportScale: Double = 1.0,
	//val exportScale: Double = 1.1,
    val minShapeSide: Int = 64,
    val maxShapeSide: Int = 512,
    val minMorphShapeSide: Int = 16,
    val maxMorphShapeSide: Int = 128,
    val maxTextureSide: Int = 4096,
    val exportPaths: Boolean = false,
    val adaptiveScaling: Boolean = true,
    val smoothInterpolation: Boolean = true,
    val atlasPacking: Boolean = true,
    val roundDecimalPlaces: Int = -1,
    val generateTextures: Boolean = false,
    //val generateTextures: Boolean = true,
    val graphicsRenderer: GraphicsRenderer = GraphicsRenderer.SYSTEM,
)

fun SWFExportConfig.toAnLibrarySerializerConfig(compression: Double = 1.0): AnLibrarySerializer.Config =
	AnLibrarySerializer.Config(
		compression = compression,
		keepPaths = this.exportPaths,
		mipmaps = this.mipmaps
	)

suspend fun VfsFile.readSWF(context: AnLibrary.Context, config: SWFExportConfig?): AnLibrary {
	return if (config != null) this.readSWF(context, config) else this.readSWF(context)
}

@ThreadLocal
var AnLibrary.swfExportConfig by Extra.Property { SWFExportConfig() }

suspend fun VfsFile.readSWF(
    views: Views,
    defaultConfig: SWFExportConfig = SWFExportConfig(),
    atlasPacking: Boolean? = null
): AnLibrary = readSWF(AnLibrary.Context(views), defaultConfig.copy(atlasPacking = atlasPacking ?: defaultConfig.atlasPacking))

val ShapeRasterizerMethodValues = ShapeRasterizerMethod.values().associateBy { it.name }
val GraphicsRendererValues = GraphicsRenderer.values().associateBy { it.name }

suspend fun VfsFile.readSWF(
    context: AnLibrary.Context,
    content: ByteArray? = null,
    defaultConfig: SWFExportConfig = SWFExportConfig()
): AnLibrary {
	val configFile = this.appendExtension("config")
	val config = try {
		if (configFile.exists()) {
			val data = Yaml.decode(configFile.readString()).dyn
			SWFExportConfig(
				debug = data["debug"].toBoolOrNull() ?: false,
				mipmaps = data["mipmaps"].toBoolOrNull() ?: true,
				antialiasing = data["antialiasing"].toBoolOrNull() ?: true,
				rasterizerMethod = ShapeRasterizerMethodValues[data["rasterizerMethod"].toStringOrNull() ?: "X4"] ?: ShapeRasterizerMethod.X4,
				exportScale = data["exportScale"].toDoubleOrNull() ?: 1.0,
				minShapeSide = data["minShapeSide"].toIntOrNull() ?: 64,
				maxShapeSide = data["maxShapeSide"].toIntOrNull() ?: 512,
				minMorphShapeSide = data["minMorphShapeSide"].toIntOrNull() ?: 16,
				maxMorphShapeSide = data["maxMorphShapeSide"].toIntOrNull() ?: 128,
				maxTextureSide = data["maxTextureSide"].toIntOrNull() ?: 4096,
				exportPaths = data["exportPaths"].toBoolOrNull() ?: false,
				adaptiveScaling = data["adaptiveScaling"].toBoolOrNull() ?: true,
				smoothInterpolation = data["smoothInterpolation"].toBoolOrNull() ?: true,
				atlasPacking = data["atlasPacking"].toBoolOrNull() ?: true,
				roundDecimalPlaces = data["roundDecimalPlaces"].toIntOrNull() ?: -1,
				generateTextures = data["generateTextures"].toBoolOrNull() ?: true,
				graphicsRenderer = GraphicsRendererValues[data["graphicsRenderer"].toStringOrNull() ?: "SYSTEM"] ?: GraphicsRenderer.SYSTEM,
			)
		} else {
			defaultConfig
		}
	} catch (e: Throwable) {
		e.printStackTrace()
		SWFExportConfig()
	}
	val lib = readSWF(context.copy(coroutineContext = coroutineContext), config, content)
	lib.swfExportConfig = config
	return lib
}

suspend fun VfsFile.readSWF(context: AnLibrary.Context, config: SWFExportConfig, content: ByteArray? = null): AnLibrary =
	SwfLoaderMethod(context, config).load(content ?: this.readAll())

inline val TagPlaceObject.depth0: Int get() = this.depth - 1
inline val TagPlaceObject.clipDepth0: Int get() = this.clipDepth - 1
inline val TagRemoveObject.depth0: Int get() = this.depth - 1

@ThreadLocal
val SWF.bitmaps by Extra.Property { hashMapOf<Int, Bitmap>() }

class MySwfFrame(val index0: Int, maxDepths: Int) {
	var name: String? = null
	val depths = arrayListOf<AnSymbolTimelineFrame>()
	val actions = arrayListOf<Action>()

	interface Action {
		object Stop : Action
		object Play : Action
		class Goto(val frame0: Int) : Action
		class PlaySound(val soundId: Int) : Action
	}

	val isFirst: Boolean get() = index0 == 0
	val hasStop: Boolean get() = Action.Stop in actions
	val hasGoto: Boolean get() = actions.any { it is Action.Goto }
	val hasFlow: Boolean get() = hasStop || hasGoto

	fun stop() { actions += Action.Stop }
	fun play() { actions += Action.Play }
	fun goto(frame: Int) { actions += Action.Goto(frame) }
	fun gotoAndStop(frame: Int) { goto(frame); stop() }
	fun gotoAndPlay(frame: Int) { goto(frame); play() }
	fun playSound(soundId: Int) { actions += Action.PlaySound(soundId) }
}

class MySwfTimeline {
	val frames = arrayListOf<MySwfFrame>()
}

@ThreadLocal
internal val AnSymbolMovieClip.swfTimeline by Extra.Property { MySwfTimeline() }
@ThreadLocal
internal val AnSymbolMovieClip.labelsToFrame0 by Extra.Property { hashMapOf<String, Int>() }

@ThreadLocal
var AnSymbolMorphShape.tagDefineMorphShape by Extra.Property<TagDefineMorphShape?> { null }
@ThreadLocal
var AnSymbolShape.tagDefineShape by Extra.Property<TagDefineShape?> { null }
