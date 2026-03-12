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

class TrophyNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val trophyNode = node as? Node.Trophy ?: return null

        emit(GameEvent.UnlockTrophy(trophyNode.trophyId))

        return null // Let engine find next via edges
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class TrophyNodeHandlerModule {
    @Binds
    @IntoMap
    @NodeTypeKey(NodeType.TROPHY)
    abstract fun bindHandler(handler: TrophyNodeHandler): NodeHandler
}
