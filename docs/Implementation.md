This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*
Do never delete implementation.md.

You are an expert and smart UX/UI expert.
This operation is done because we need to save tokens for our AI models-the files are too long.

# Update Sound Engine in GameScreen. [DONE - branch feature/stop-sound-node]

- [DONE] New node type `stop-sound` (`{"type":"stop-sound","data":{"targetNodeId":"<sound-node-id>"}}`)
  fades out and clears the sound started by the targeted sound node; clearing a sound that is
  not playing fails silently.
  - Model/engine: `Node.StopSound`, `NodeType.STOP_SOUND`, `StopSoundNodeHandler`,
    `HandlerEffect.StopSound(targetNodeId)`; `PlaySound` now carries the originating `nodeId`.
  - Parser: `ChapterGraphParser` maps `stop-sound` (node dropped when `targetNodeId` is missing/blank).
  - Sound engine (`GameAudioController`): sound channels are keyed by sound-node id;
    `stopSound(targetNodeId)` cancels any pending delayed playback for that node and fades the
    channel out (600 ms ramp) before releasing it. Ambient loop replacement behavior is unchanged.
  - Cinematics: `stop-sound` is cinematic-playable; `CinematicScreen` clears its own sound when targeted.
  - Validated in debug with no cache: `:game:domain` / `:game:data` unit tests all green
    (new `ChapterGraphParserStopSoundTest`, `StopSoundNodeHandlerTest`), `:app:compileDebugKotlin` OK.