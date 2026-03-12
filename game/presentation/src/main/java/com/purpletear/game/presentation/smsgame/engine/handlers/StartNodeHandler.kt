package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.game.presentation.smsgame.engine.NodeType
import com.purpletear.game.presentation.smsgame.engine.NodeTypeKey
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import javax.inject.Inject

class StartNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val startNode = node as? Node.Start ?: return null
        return getNextNodeId(node.id, memory)
    }

    private fun getNextNodeId(currentId: String, memory: GameMemory): String? {
        // This will be resolved by the GameEngine based on edges
        // For now, return null to let the engine handle it
        return null
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class StartNodeHandlerModule {
    @Binds
    @IntoMap
    @NodeTypeKey(NodeType.START)
    abstract fun bindHandler(handler: StartNodeHandler): NodeHandler
}
