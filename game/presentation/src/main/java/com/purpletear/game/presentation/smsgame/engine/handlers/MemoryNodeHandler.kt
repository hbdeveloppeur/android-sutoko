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

class MemoryNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val memoryNode = node as? Node.Memory ?: return null

        memory.set(memoryNode.key, memoryNode.value)

        return null // Let engine find next via edges
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MemoryNodeHandlerModule {
    @Binds
    @IntoMap
    @NodeTypeKey(NodeType.MEMORY)
    abstract fun bindHandler(handler: MemoryNodeHandler): NodeHandler
}
