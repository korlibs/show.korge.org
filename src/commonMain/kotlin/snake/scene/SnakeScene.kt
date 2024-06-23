package snake.scene

import extension.*
import korlibs.korge.scene.*
import korlibs.korge.view.*

class SnakeScene : AutoShowScene() {
    override suspend fun SContainer.main() {
        sceneContainer().changeTo { IngameScene() }
    }
}