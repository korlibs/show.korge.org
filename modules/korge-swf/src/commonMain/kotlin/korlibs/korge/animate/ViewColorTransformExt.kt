package korlibs.korge.animate

import korlibs.image.color.*
import korlibs.korge.view.*

// Now KorGE doesn't support additive color transform directly without filters. For now, let's ignore it.
// Eventually we could figure out a way to support it when used.

var View.colorTransform: ColorTransform
    get() = ColorTransform(colorMul, ColorAdd.NEUTRAL)
    set(value) {
        colorMul = value.colorMul
    }

val View.renderColorAdd: ColorAdd get() = ColorAdd.NEUTRAL

