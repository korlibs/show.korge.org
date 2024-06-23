package com.esotericsoftware.spine

import korlibs.image.atlas.*
import korlibs.image.bitmap.*
import korlibs.image.format.ImageOrientation
import korlibs.math.geom.slice.*

class SpineRegion(val entry: Atlas.Entry) {
    val bmpSlice: BmpSlice = entry.slice
    val bmp: Bitmap = bmpSlice.base
    val coords: RectCoords = bmpSlice.coords
    val texture: Bitmap = bmp
    var rotate: Boolean = entry.info.imageOrientation != ImageOrientation.ORIGINAL
    val u: Float = coords.tlX
    val u2: Float = coords.brX
    val v: Float = if (rotate) bmpSlice.brY else coords.tlY
    val v2: Float = if (rotate) bmpSlice.tlY else coords.brY
    var offsetX: Float = (entry.info.virtFrame?.x ?: 0).toFloat()
    var offsetY: Float = (entry.info.virtFrame?.y ?: 0).toFloat()
    var originalWidth: Float = (entry.info.virtFrame?.width ?: entry.info.frame.width).toFloat()
    var originalHeight: Float = (entry.info.virtFrame?.height ?: entry.info.frame.height).toFloat()
    var packedWidth: Float = entry.info.frame.width.toFloat()
    var packedHeight: Float = entry.info.frame.height.toFloat()
    var degrees: Int = if (rotate) 90 else 0
}
