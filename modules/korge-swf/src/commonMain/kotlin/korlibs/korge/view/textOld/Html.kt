package korlibs.korge.view.textOld

import korlibs.encoding.*
import korlibs.datastructure.*
import korlibs.datastructure.iterators.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.image.text.*
import korlibs.io.async.*
import korlibs.io.serialization.xml.*
import korlibs.io.stream.*
import korlibs.korge.bitmapfont.*
import korlibs.math.geom.*
import kotlin.coroutines.*
import kotlin.native.concurrent.*

private val DEBUG_FONT_BYTES: ByteArray get() = "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".fromBase64()

@ThreadLocal // It is mutable because of the Extra, so can't use SharedImmutable
private var debugBmpFontOrNull: BitmapFont? = null

private fun debugBmpFont(tex: BmpSlice): BitmapFont {
    val fntAdvance = 7.0
    val fntWidth = 8.0
    val fntHeight = 8.0

    val fntBlockX = 2.0
    val fntBlockY = 2.0
    val fntBlockWidth = 12.0
    val fntBlockHeight = 12.0

    return BitmapFont(fntHeight, fntHeight, fntHeight - 1f, (0 until 256).associateWith {
        val x = it % 16
        val y = it / 16
        BitmapFont.Glyph(
            fntHeight,
            it,
            tex.sliceWithSize(
                (x * fntBlockWidth + fntBlockX).toInt(),
                (y * fntBlockHeight + fntBlockY).toInt(),
                fntWidth.toInt(), fntHeight.toInt()
            ),
            0, 0, fntAdvance.toInt()
        )
    }.toIntMap(), IntMap())
}

val debugBmpFontSync: BitmapFont get() {
    if (debugBmpFontOrNull == null) {
        debugBmpFontOrNull = debugBmpFont(PNG.decode(DEBUG_FONT_BYTES).slice())
    }
    return debugBmpFontOrNull!!
}

@ThreadLocal
private var _DefaultFontsCatalog: Html.FontsCatalog? = null

object Html {
    class FontsCatalog(val default: Font?, val context: CoroutineContext? = null, val fonts: Map<String?, Font?> = hashMapOf()) :
        MetricsProvider {
        open val defaultFont: BitmapFont get() = debugBmpFontSync
        fun String.normalize() = this.lowercase().trim()
        fun registerFont(name: String, font: Font) { (fonts as MutableMap<String, Font>)[name.normalize()] = font }
        fun getBitmapFontOrNull(name: String?, font: Font? = null): Font? =
            fonts[name?.normalize()] ?: font ?: default ?: context?.let { SystemFont(name ?: "default", it) }
        fun getBitmapFont(name: String?, font: Font? = null): Font =
            getBitmapFontOrNull(name, font) ?: defaultFont
        fun getBitmapFont(font: Font?): BitmapFont {
            if (font is BitmapFont) return font
            val fontName = font?.name?.normalize()
            (fonts[fontName] as? BitmapFont)?.let { return it }
            return (fonts as MutableMap<String?, Font?>)?.getOrPut("$fontName.\$BitmapFont") {
                font?.toBitmapFont(32f)
            } as? BitmapFont? ?: defaultFont
        }
        override fun getBounds(text: String, format: Format, out: MRectangle) {
            val font = format.computedFace
            getBitmapFont(font?.name, font).getBounds(text, format, out)
        }
    }

    val DefaultFontCatalogWithoutSystemFonts: FontsCatalog = FontsCatalog(null, null)
    fun DefaultFontsCatalog(coroutineContext: CoroutineContext): FontsCatalog = cacheLazyNullable(::_DefaultFontsCatalog) { FontsCatalog(null, coroutineContext, mapOf()) }
    suspend fun DefaultFontsCatalog(): FontsCatalog = DefaultFontsCatalog(coroutineContext)

    data class Format(
        override var parent: Format? = null,
        var color: RGBA? = null,
        var face: Font? = null,
        var size: Int? = null,
        var letterSpacing: Double? = null,
        var kerning: Int? = null,
        var align: TextAlignment? = null
    ) : Computed.WithParent<Format> {
        //java.lang.ClassCastException: korlibs.image.color.RGBA cannot be cast to java.lang.Number
        //	at com.soywiz.korge.html.Html$Format.getComputedColor(Html.kt)
        //	at com.soywiz.korge.view.Text.render(Text.kt:134)
        //	at com.soywiz.korge.view.TextTest.testRender(TextTest.kt:12)
        //val computedColor by Computed(Format::color) { Colors.WHITE }

        val computedColor: RGBA get() = parent?.computedColor ?: color ?: Colors.WHITE

        //val computedFace by Computed(Format::face) { FontFace.Named("Arial") }
        val computedFace by Computed(Format::face) { null }
        val computedSize by Computed(Format::size) { 16 }
        val computedLetterSpacing by Computed(Format::letterSpacing) { 0.0 }
        val computedKerning by Computed(Format::kerning) { 0 }
        val computedAlign by Computed(Format::align) { TextAlignment.LEFT }

        fun consolidate(): Format = Format(
            parent = null,
            color = computedColor,
            face = computedFace,
            size = computedSize,
            letterSpacing = computedLetterSpacing,
            kerning = computedKerning,
            align = computedAlign
        )
    }

    interface MetricsProvider {
        fun getBounds(text: String, format: Format, out: MRectangle): Unit

        object Identity : MetricsProvider {
            override fun getBounds(text: String, format: Format, out: MRectangle) {
                out.setTo(0.0, 0.0, text.length.toDouble(), 1.0)
            }
        }
    }

    data class PositionContext(
        val provider: MetricsProvider,
        val bounds: MRectangle,
        var x: Double = 0.0,
        var y: Double = 0.0
    )

    data class Span(val format: Format, var text: String) : Extra by Extra.Mixin() {
        val bounds = MRectangle()

        fun doPositioning(ctx: PositionContext) {
            ctx.provider.getBounds(text, format, bounds)
            bounds.x += ctx.x
            ctx.x += bounds.width
        }
    }

    data class Line(val spans: ArrayList<Span> = arrayListOf()) : Extra by Extra.Mixin() {
        var format: Format = Format()
        val firstNonEmptySpan get() = spans.firstOrNull { it.text.isNotEmpty() }
        val bounds = MRectangle()

        fun doPositioning(ctx: PositionContext) {
            ctx.x = ctx.bounds.x
            spans.fastForEach { v ->
                // @TODO: Reposition when overflowing
                v.doPositioning(ctx)
            }

            spans.map { it.bounds }.bounds(bounds) // calculate bounds

            // Alignment
            //println(bounds)
            val restoreY = bounds.y
            bounds.setToAnchoredRectangle(bounds, format.computedAlign.anchor, ctx.bounds)
            bounds.y = restoreY
            //println(bounds)
            var sx = bounds.x
            spans.fastForEach { v ->
                v.bounds.x = sx
                sx += v.bounds.width
            }

            ctx.x = ctx.bounds.x
            ctx.y += bounds.height
        }
    }

    data class Paragraph(val lines: ArrayList<Line> = arrayListOf()) : Extra by Extra.Mixin() {
        val firstNonEmptyLine get() = lines.firstOrNull { it.firstNonEmptySpan != null }
        val bounds = MRectangle()

        fun doPositioning(ctx: PositionContext) {
            lines.fastForEach { v ->
                v.doPositioning(ctx)
            }
            lines.map { it.bounds }.bounds(bounds) // calculate bounds
            ctx.x = bounds.left
            ctx.y = bounds.bottom
        }
    }

    data class Document(val paragraphs: ArrayList<Paragraph> = arrayListOf()) : Extra by Extra.Mixin() {
        val defaultFormat = Format()
        var xml = Xml("")
        val text: String get() = xml.text.trim()
        val bounds = MRectangle()
        val firstNonEmptyParagraph get() = paragraphs.firstOrNull { it.firstNonEmptyLine != null }
        val firstNonEmptySpan get() = firstNonEmptyParagraph?.firstNonEmptyLine?.firstNonEmptySpan
        val firstFormat get() = firstNonEmptySpan?.format ?: Format()
        val allSpans get() = paragraphs.flatMap { it.lines }.flatMap { it.spans }

        fun doPositioning(gp: MetricsProvider, bounds: MRectangle) {
            val ctx = PositionContext(gp, bounds)
            paragraphs.fastForEach { v ->
                v.doPositioning(ctx)
            }
            paragraphs.map { it.bounds }.bounds(this.bounds) // calculate bounds
        }
    }

    class HtmlParser(val fontsCatalog: FontsCatalog?) {
        val document = Document()
        var currentLine = Line()
        var currentParagraph = Paragraph()

        val Xml.isDisplayBlock get() = this.name == "p" || this.name == "div"

        fun emitText(format: Format, text: String) {
            //println(format)
            //println(text)
            if (currentLine.spans.isEmpty()) {
                currentLine.format = Format(format)
            }
            currentLine.spans += Span(Format(format), text)
        }

        fun emitEndOfLine(format: Format) {
            //println("endOfLine")
            if (currentLine.spans.isNotEmpty()) {
                //currentLine.format = format
                currentParagraph.lines += currentLine
                document.paragraphs += currentParagraph
                currentParagraph = Paragraph()
                currentLine = Line()
            }
        }

        fun parse(xml: Xml, format: Format): Format {
            when {
                xml.isText -> {
                    emitText(format, xml.text)
                }
                xml.isComment -> Unit
                xml.isNode -> {
                    val block = xml.isDisplayBlock
                    format.align = when (xml.str("align").toLowerCase()) {
                        "center" -> TextAlignment.CENTER
                        "left" -> TextAlignment.LEFT
                        "right" -> TextAlignment.RIGHT
                        "jusitifed" -> TextAlignment.JUSTIFIED
                        else -> format.align
                    }
                    val face = xml.strNull("face")
                    format.face = if (face != null) fontsCatalog?.getBitmapFont(face) else format.face
                    format.size = xml.intNull("size") ?: format.size
                    format.letterSpacing = xml.doubleNull("letterSpacing") ?: format.letterSpacing
                    format.kerning = xml.intNull("kerning") ?: format.kerning
                    format.color = Colors[xml.strNull("color") ?: "white"]
                    xml.allChildrenNoComments.fastForEach { child ->
                        // @TODO: Change .copy for an inline format.keep { parse(xml, format) } that doesn't allocate at all
                        parse(child, Format(format))
                    }
                    if (block) {
                        emitEndOfLine(format)
                    }
                }
            }
            return format
        }

        fun parse(html: String) {
            val xml = Xml(html)
            document.xml = xml
            //println(html)
            val format = parse(xml, document.defaultFormat)
            emitEndOfLine(format)
            //println(document.firstFormat)
        }
    }

    fun parse(html: String, fontsCatalog: FontsCatalog?): Document = HtmlParser(fontsCatalog).apply { parse(html) }.document
}
