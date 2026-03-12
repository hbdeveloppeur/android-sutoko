package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.game.presentation.smsgame.engine.NodeType
import com.purpletear.game.presentation.smsgame.engine.NodeTypeKey
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import javax.inject.Inject

class ChoiceNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val choiceNode = node as? Node.Choice ?: return null

        val options = choiceNode.options.map { it.text }
        
        emit(GameEvent.WaitingForInput)
        
        // Return a sentinel value to indicate waiting for user input
        // The actual navigation will be handled by selectChoice()
        return WAITING_FOR_INPUT
    }

    companion object {
        const val WAITING_FOR_INPUT = "__WAITING__"
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ChoiceNodeHandlerModule {
    @Binds
    @IntoMap
    @NodeTypeKey(NodeType.CHOICE)
    abstract fun bindHandler(handler: ChoiceNodeHandler): NodeHandler
}
