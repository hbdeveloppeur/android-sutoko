This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

# Fix 1: Analyze the archive "/Users/hb/Temporaire/stories_qOVSbMIn022_archives_preview/" and tell us-given the current code- will the character Kuum be displayed on the right side of the screen for chapter 1a?

## Answer (analysis/kuum-side-chapter-1a): No — Kuum will be displayed on the LEFT side.

Reasoning:
- Bubble side is decided in `game/presentation/.../game_play/SmsGameScreen.kt` (l.162-171):
  if `state.rightSideCharacterIds` is non-empty, a message goes right only if its
  characterId is in that set; otherwise it falls back to `character?.isMainCharacter == true`.
- In `characters/characters.json`, Kuum is id 7171 with `isMainCharacter: false`
  (only "You", id 7166, is main). So the fallback rule puts Kuum on the LEFT
  (`Alignment.CenterStart`, `MessageDest` in `components/message/MessageText.kt`).
- The override `rightSideCharacterIds` comes ONLY from the remote chapter metadata
  (`ChapterDto.layout.sides.right`, mapped in `game/data/.../remote/dto/ChapterDto.kt`,
  persisted in Room `ChapterEntity`). The archive preview itself contains no layout/sides
  data (only nodes.json/edges.json per chapter), so no override applies here.
- Kuum has 33 message nodes in chapter 1a; characterId 7171 resolves fine in
  `characters.json`, so no null-character crash path is hit. One Kuum node
  (`message-1785888632915`) has an empty text: the engine skips blank messages
  (`MessageNodeHandler` l.84-88), so it renders nothing.

Conclusion: with the current code and this archive, Kuum appears on the LEFT side for
chapter 1a. Kuum would only appear on the right if the server sent
`layout.sides.right: [..., 7171, ...]` in the chapter metadata — and in that case any
non-main character not listed (including "You") would flip sides.

# Fix 2: Now a Sound Node may have a float "volume" field (optional). So you will have to change the players in SmsGameActivity

# Fix 3: In click to progress mode when playing a story, some node must not need a click to progress (Scene node must auto appear and continue, sounds too)