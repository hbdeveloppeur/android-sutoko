package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.*
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf

internal fun checkpointChoiceGraph() = ChapterGraph(
    chapterCode="8A",chapterNumber=8,title="Choice checkpoint",
    nodes=listOf(Node.Start("start"),Node.Message("stay","Stay",1),Node.Message("leave","Leave",1),
        Node.Memory("remember","IStayed","true"),Node.Message("after","A later message",2),Node.End("end")).associateBy { it.id },
    edges=listOf(Edge("start","stay",EdgeType.NORMAL),Edge("start","leave",EdgeType.NORMAL),
        Edge("stay","remember",EdgeType.NORMAL),Edge("remember","after",EdgeType.NORMAL),
        Edge("leave","after",EdgeType.NORMAL),Edge("after","end",EdgeType.NORMAL)),startNodeId="start"
)
internal fun linearCheckpointGraph(vararg nodes:Node):ChapterGraph {
    val all=listOf(Node.Start("start"))+nodes
    return ChapterGraph("8A",8,"Gate checkpoint",all.associateBy { it.id },
        all.zipWithNext { a,b->Edge(a.id,b.id,EdgeType.NORMAL) },"start")
}
internal class CheckpointTestStore {
    var cancelAtGate:GameCheckpoint.Gate?=null
    var saved=UserGameProgress(gameId="story",currentChapterCode="8A",normalizedChapterCode="8a")
    val progress=object:UserGameProgressRepository {
        override fun observe(gameId:String)=flowOf(saved)
        override suspend fun get(gameId:String)=saved
        override suspend fun save(progress:UserGameProgress){
            if(cancelAtGate!=null && progress.checkpoint?.gate==cancelAtGate)throw CancellationException("interrupted save")
            saved=progress
        }
        override suspend fun delete(gameId:String){saved=UserGameProgress(gameId=gameId)}
    }
    private val entries=mutableMapOf<Pair<String,Int>,MemoryEntry>()
    val memories=object:MemoryRepository {
        override suspend fun load(gameId:String,upToChapterNumber:Int):Map<String,MemoryEntry>{
            entries.keys.removeAll { it.second>=upToChapterNumber }
            return entries.entries.groupBy { it.key.first }.mapValues { (_,v)->v.maxBy { it.key.second }.value }
        }
        override suspend fun save(gameId:String,memories:Map<String,MemoryEntry>){
            memories.forEach { (key,entry)->entries[key to entry.chapterNumber]=entry }
        }
        override suspend fun clear(gameId:String){entries.clear()}
        override suspend fun delete(gameId:String){entries.clear()}
        override fun observe(gameId:String)=flowOf(entries.entries.associate { it.key.first to it.value.value })
        override suspend fun upsert(gameId:String,key:String,value:String,chapterNumber:Int){entries[key to chapterNumber]=MemoryEntry(value,chapterNumber)}
    }
}
