package spine

import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.korge.*
import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.*
import com.soywiz.korim.color.*
import com.soywiz.korio.file.std.*
import extension.*

class SpineScene : ShowScene() {
    override suspend fun Container.sceneMain() {
        val baseVfs = resourcesVfs["spine/spineboy"]
        val atlas = baseVfs["spineboy-pma.atlas"].readAtlas(asumePremultiplied = true)
        val skeletonData = baseVfs["spineboy-pro.skel"].readSkeletonBinary(atlas, 0.4f)

        val skeleton = Skeleton(skeletonData) // Skeleton holds skeleton state (bone positions, slot attachments, etc).
        //skeleton.setPosition(250f, 20f)

        val stateData = AnimationStateData(skeletonData) // Defines mixing (crossfading) between animations.
        stateData.setMix("run", "jump", 0.2f)
        stateData.setMix("jump", "run", 0.2f)

        val state = AnimationState(stateData) // Holds the animation state for a skeleton (current animation, time, etc).
        state.timeScale = 1.1f // Speed ups all animations down to 110% speed.

        // Queue animations on track 0.
        state.setAnimation(0, "run", true)
        state.addAnimation(0, "jump", false, 2f) // Jump after 2 seconds.
        state.addAnimation(0, "run", true, 0f) // Run after the jump.

        state.update(1f / 60f) // Update the animation time.

        state.apply(skeleton) // Poses skeleton using current animations. This sets the bones' local SRT.
        skeleton.updateWorldTransform() // Uses the bones' local SRT to compute their world SRT.

        // Add view
        container {
            //speed = 2.0
            speed = 1.0
            scale(2.0)
            position(views.virtualWidth * 0.5, views.virtualHeight * 0.9)
            skeletonView(skeleton, state)
            solidRect(10.0, 10.0, Colors.RED).centered
            mouseEnabled = false
        }
    }
}
