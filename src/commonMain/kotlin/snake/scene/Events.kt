package snake.scene

import korlibs.event.*
import snake.model.*

open class GameInfoUpdatedEvent(val gameInfo: GameInfo) : TypedEvent<GameInfoUpdatedEvent>(GameInfoUpdatedEvent) {
    companion object : EventType<GameInfoUpdatedEvent>
}

open class GameStartEvent() : TypedEvent<GameStartEvent>(GameStartEvent) {
    companion object : EventType<GameStartEvent>
}
