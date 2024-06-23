package snake.scene

import korlibs.korge.time.delay
import korlibs.korge.view.Container
import korlibs.korge.view.SContainer
import korlibs.korge.view.fixedSizeContainer
import korlibs.time.milliseconds

typealias StateFunc = suspend (Container) -> Unit

interface StateScene {
    var changeState: StateFunc?
    class Mixin : StateScene {
        override var changeState: StateFunc? = null
    }

    suspend fun SContainer.runStates(startingFunc: StateFunc) {
        var func: StateFunc = startingFunc
        while (true) {
            val stateView = fixedSizeContainer(this.size)
            try {
                func(stateView)
                while (true) frame()
            } catch (e: ChangeSceneException) {
                func = e.func
            } finally {
                stateView.removeFromParent()
            }
        }
    }

    fun change(func: StateFunc) {
        changeState = func
        //throw ChangeSceneException(func)
    }

    class ChangeSceneException(val func: StateFunc) : Throwable()

    suspend fun Container.frame() {
        val newState = changeState
        if (newState != null) {
            changeState = null
            throw ChangeSceneException(newState)
        }
        delay(16.milliseconds)
    }
}
