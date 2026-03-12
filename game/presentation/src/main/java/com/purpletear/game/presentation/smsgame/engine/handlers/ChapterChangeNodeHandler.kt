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

class ChapterChangeNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val chapterChangeNode = node as? Node.ChapterChange ?: return null
        emit(GameEvent.ChangeChapter(chapterChangeNode.chapterCode))
        return null
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ChapterChangeNodeHandlerModule {
    @Binds
    @IntoMap
    @NodeTypeKey(NodeType.CHAPTER_CHANGE)
    abstract fun bindHandler(handler: ChapterChangeNodeHandler): NodeHandler
}
