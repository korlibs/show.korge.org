package korlibs.korge.view.textOld

import korlibs.image.font.*
import korlibs.math.geom.*
import kotlin.math.*

fun Font.getBounds(text: String, format: Html.Format, out: MRectangle = MRectangle()): MRectangle {
    //val font = getBitmapFont(format.computedFace, format.computedSize)
    val font = this
    val textSize = format.computedSize.toDouble()
    var width = 0.0
    var height = 0.0
    var dy = 0.0
    var dx = 0.0
    val glyph = GlyphMetrics()
    val fmetrics = font.getFontMetrics(textSize)
    for (n in 0 until text.length) {
        val c1 = text[n].toInt()
        if (c1 == '\n'.toInt()) {
            dx = 0.0
            dy += fmetrics.lineHeight
            height = max(height, dy)
            continue
        }
        var c2: Int = ' '.toInt()
        if (n + 1 < text.length) c2 = text[n + 1].toInt()
        val kerningOffset = font.getKerning(textSize, c1, c2)
        val glyph = font.getGlyphMetrics(textSize, c1, glyph)
        dx += glyph.xadvance + kerningOffset
        width = max(width, dx)
    }
    height += fmetrics.lineHeight
    //val scale = textSize / font.fontSize.toDouble()
    //out.setTo(0.0, 0.0, width * scale, height * scale)
    out.setTo(0.0, 0.0, width, height)
    return out
}
