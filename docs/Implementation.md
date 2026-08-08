This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

You are an expert in UX/UI design.

# Update VisualNovelOverlay ✅

Done (branch feat/visual-novel-node): new dialog structure supported — `duration`/`delay`
now authored in ms (`delayMs`, `durationMs` on `Node.VisualNovel.Dialog`), optional per-dialog
sound (`soundPath` resolved locally or via remote fallback; `soundDurationMs` used as duration
fallback). The overlay honors each dialog's delay, fires `onDialogSound` when a sounded dialog
appears (ViewModel one-shot `MediaPlayer`, async prepare), and highlights its words karaoke-style
over the duration. Validated: `:game:data` + `:game:domain` unit tests (6/6 and 4/4) and
`:game:presentation:compileDebugKotlin`, debug, no cache.

We changed the way VisualNovel node works as you can see in the new structure example below.
Make the changes. And optionnaly, a dialog can be played with a sound-if so, the words are animated
by getting highlighted smoothly following the duration.

The new structure example: {"id":"visual-novel-1786145270008","type":"visual-novel","data":{"
title":"Inconnue","
dialogs":[{"text":"H\u00e9, vous faites quoi ? Vous voulez que je cache le cadavre toute seule ?!","duration":2430,"delay":1000,"soundAssetId":4584,"soundStoragePath":"uploads\/uploads\/sounds\/2026\/08\/5bfeefa7-9a01-49de-b935-b36d310b0656.mp3","soundName":"1.mp3","soundDurationMs":2430},{"text":"Prenez une pelle et aidez-moi, tout de suite !","delay":0,"duration":1301,"soundAssetId":4585,"soundStoragePath":"uploads\/uploads\/sounds\/2026\/08\/84371ab7-3062-4850-bc99-c723bab3972b.mp3","soundName":"2.mp3","soundDurationMs":1301}],"
sounds":[],"
layers":[{"assetId":4578,"storagePath":"uploads\/videos\/2026\/08\/f3ea6d26-8923-4c46-86e3-8997d5e801a9.mp4","thumbnailStoragePath":"uploads\/videos\/2026\/08\/f3ea6d26-8923-4c46-86e3-8997d5e801a9.webp","name":"compare-v3_0_turbo-loop.mp4","type":"video"},{"assetId":4581,"storagePath":"uploads\/images\/2026\/08\/197be109-5ecb-4dea-9ead-63d02264769c.webp","thumbnailStoragePath":"uploads\/thumbnails\/images\/2026\/08\/1ef070d5-97f7-43a9-8071-459129e81602.webp","type":"image"}],"
theme":{"color":"#332F63","opacity":1},"delay":0}}