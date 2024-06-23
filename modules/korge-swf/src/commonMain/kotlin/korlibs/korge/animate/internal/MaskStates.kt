package korlibs.korge.animate.internal

import korlibs.korge.render.*
import korlibs.graphics.*

@OptIn(korlibs.korge.internal.KorgeInternal::class)
object MaskStates {
    class RenderState(val stencilRef: AGStencilReference, val stencilOpFunc: AGStencilOpFunc, val colorMask: AGColorMask) {
        @Suppress("DEPRECATION")
        fun set(ctx: RenderContext, referenceValue: Int) {
            ctx.flush()
            if (ctx.masksEnabled) {
                ctx.batch.stencilRef = stencilRef.withReferenceValue(referenceValue)
                ctx.batch.stencilOpFunc = stencilOpFunc
                ctx.batch.colorMask = colorMask
            } else {
                ctx.batch.stencilRef = STATE_NONE.stencilRef
                ctx.batch.stencilOpFunc = STATE_NONE.stencilOpFunc
                ctx.batch.colorMask = STATE_NONE.colorMask
            }
        }
    }

    val STATE_NONE = RenderState(
        AGStencilReference.DEFAULT,
        AGStencilOpFunc.DEFAULT.withEnabled(false),
        AGColorMask(true, true, true, true)
    )
    val STATE_SHAPE = RenderState(
        AGStencilReference.DEFAULT.withReadMask(0x00).withWriteMask(0xFF).withReferenceValue(0),
        AGStencilOpFunc.DEFAULT.withEnabled(true).withCompareMode(AGCompareMode.ALWAYS).withActionOnBothPass(AGStencilOp.SET).withActionOnDepthFail(AGStencilOp.SET).withActionOnDepthPassStencilFail(AGStencilOp.SET),
        AGColorMask(false, false, false, false)
    )
    val STATE_CONTENT = RenderState(
        AGStencilReference.DEFAULT.withReadMask(0xFF).withWriteMask(0x00).withReferenceValue(0),
        AGStencilOpFunc.DEFAULT.withEnabled(true).withCompareMode(AGCompareMode.EQUAL).withActionOnBothPass(AGStencilOp.KEEP).withActionOnDepthFail(AGStencilOp.KEEP).withActionOnDepthPassStencilFail(AGStencilOp.KEEP),
        AGColorMask(true, true, true, true)
    )
}
