package snake.scene

import korlibs.korge.animate.*
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.alpha
import korlibs.korge.view.text
import korlibs.korge.view.xy
import snake.model.*

class OverlayScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val scoreText = text("").xy(8, 8)
        var current = GameInfo()
        var hiScoreBeatShown = false

        fun updateGameInfo(old: GameInfo, new: GameInfo) {
            scoreText.text = "Score: ${new.score}, HI-Score: ${new.hiScore}"
            if (new.hiScore > old.hiScore && !hiScoreBeatShown) {
                hiScoreBeatShown = true
                val text = sceneView.text("Hi-Score beaten", textSize = 32.0).centerOnStage().alpha(0.0)
                sceneView.animator {
                    parallel {
                        show(text)
                        this.moveBy(text, 0.0, 32.0)
                    }
                    hide(text)
                    block { text.removeFromParent() }
                }
            }
        }

        onEvent(GameStartEvent) {
            hiScoreBeatShown = false
        }

        onEvent(GameInfoUpdatedEvent) {
            updateGameInfo(current, it.gameInfo)
            current = it.gameInfo
        }
    }
}
