package korlibs.korge.view

import korlibs.korge.animate.serialization.readAnimation
import korlibs.korge.render.RenderContext
import korlibs.io.file.VfsFile
import korlibs.io.file.extensionLC

class AnimationViewRef() : Container(), ViewLeaf, ViewFileRef by ViewFileRef.Mixin() {
    override suspend fun forceLoadSourceFile(views: Views, currentVfs: VfsFile, sourceFile: String?) {
        baseForceLoadSourceFile(views, currentVfs, sourceFile)
        removeChildren()
        addChild(currentVfs["$sourceFile"].readAnimation(views).createMainTimeLine())
    }

    override fun renderInternal(ctx: RenderContext) {
        this.lazyLoadRenderInternal(ctx, this)
        super.renderInternal(ctx)
    }

    //override fun buildDebugComponent(views: Views, container: UiContainer) {
    //    container.uiCollapsibleSection("SWF") {
    //        uiEditableValue(::sourceFile, kind = UiTextEditableValue.Kind.FILE(views.currentVfs) {
    //            it.extensionLC == "swf" || it.extensionLC == "ani"
    //        })
    //    }
    //    super.buildDebugComponent(views, container)
    //}
}
