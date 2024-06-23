package com.esotericsoftware.spine.ext

import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.korge.*
import korlibs.math.geom.*

fun Animation.getAnimationMaxBounds(skeletonData: SkeletonData, out: MRectangle = MRectangle()): MRectangle {
    val animation = this
    val skeleton = Skeleton(skeletonData)
    val skeletonView = SkeletonView(skeleton, null)
    var time = 0f
    val bb = MBoundsBuilder()
    while (time < animation.duration) {
        animation.apply(skeleton, time, time, false, null, 1f, Animation.MixBlend.replace, Animation.MixDirection.`in`)
        skeleton.updateWorldTransform()
        bb.add(skeletonView.getLocalBounds().mutable)
        time += 0.1f
    }
    return bb.getBounds(out)
}
