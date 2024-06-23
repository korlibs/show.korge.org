package com.esotericsoftware.spine.korge

import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.ext.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.image.atlas.*
import korlibs.image.format.ImageDecodingProps
import korlibs.io.file.*

class SpineViewRef() : Container(), ViewLeaf, ViewFileRef by ViewFileRef.Mixin() {
    private var skeletonView: SkeletonView? = null
    private var skeleton: Skeleton? = null

    override suspend fun forceLoadSourceFile(views: Views, currentVfs: VfsFile, sourceFile: String?) {
        baseForceLoadSourceFile(views, currentVfs, sourceFile)
        removeChildren()

        val file = currentVfs["$sourceFile"]
        val atlas = file.parent.listSimple().firstOrNull { it.baseName.endsWith(".atlas") }
            ?: error("Can't find atlas in ${file.parent}")
        val skeletonData = file.readSkeletonBinary(atlas.readAtlas(ImageDecodingProps(asumePremultiplied = true)))
        val skeleton = Skeleton(skeletonData)
        val stateData = AnimationStateData(skeletonData)
        val state = AnimationState(stateData)

        this.skeleton = skeleton
        val animationNames = skeleton.data.animations.map { it.name }

        //this.animationNames = animationNames.toSet()

        val defaultAnimationName = when {
            //"portal" in animationNames -> "portal"
            "idle" in animationNames -> "idle"
            "walk" in animationNames -> "walk"
            "run" in animationNames -> "run"
            else -> animationNames.lastOrNull() ?: "unknown"
        }

        state.setAnimation(0, defaultAnimationName, true)
        state.apply(skeleton)
        skeletonView = this.skeletonView(skeleton, state)
    }

    override fun renderInternal(ctx: RenderContext) {
        this.lazyLoadRenderInternal(ctx, this)
        super.renderInternal(ctx)
    }

    //override fun buildDebugComponent(views: Views, container: UiContainer) {
    //    container.uiCollapsibleSection("Spine") {
    //        uiEditableValue(::sourceFile, kind = UiTextEditableValue.Kind.FILE(views.currentVfs) {
    //            it.extensionLC == "skel"
    //        })
    //    }
    //    super.buildDebugComponent(views, container)
    //}
}
