This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1: As you can see in the following get Story backend response, there is a new field named "
canvasTechnologyRequiredVersion" - That field permits to know if the story is compatible with the
current app. If it is not, it means the user will have to update the app.

1. The current field "minAppBuild" seems outdated.
2. Make a plan on how to cleanly do that.

> DONE (branch `feat/canvas-technology-required-version`): `minAppBuild` replaced end-to-end by
> `canvasTechnologyRequiredVersion` (GameDto / GameCatalogEntity / GameCatalog / GameItem).
> Compatibility is now checked in `toGameActionState` against the app-side constant
> `BuildConfig.CANVAS_VERSION_COMPATIBILITY` (game/domain BuildConfig, value from the
> `canvasVersionCompatibility` property in gradle.properties, default 1) instead of
> `BuildConfig.VERSION_CODE`;
> state order: transient states (ConfirmPurchase/Pending/Downloading) first, then
> Purchase (ownership gate: the user buys first, even if the story needs a newer
> canvas), then the UpdateApp hard blocker (gates Download/UpdateGame/Play for
> owned content - no point downloading what the engine cannot run), then Download,
> UpdateGame, GameFinished, Play;
> `AppVersionProvider` removed from `GamePreviewViewModel`; Room DB bumped 15 -> 16.
> Validated: `:game:presentation` + `:game:data` unit tests and `:app:assembleDebug`
> (debug, --no-build-cache) all green.

{"id":"5JZvpvXaS6r","bannerAsset":{"originalFilename":"fpreview.jpg","width":321,"height":111,"id":
4314,"createdAt":1784002309,"fileSizeBytes":2376,"mimeType":"image\/webp","storagePath":"
uploads\/images\/2026\/07\/c3597dc0-d968-4411-9812-cce3dcdf3831.webp","thumbnailStoragePath":"
uploads\/thumbnails\/images\/2026\/07\/206575ed-eefa-4185-a569-a123ae9f4d0d.webp","image":true,"
video":false},"menuBackgroundAsset":{"originalFilename":"
stories%2F163%2Fimages%2Fsecond-preview.jpg","width":500,"height":800,"id":118,"createdAt":
1778537077,"fileSizeBytes":6040,"mimeType":"image\/webp","storagePath":"
uploads\/images\/2026\/05\/81b80196-e927-4a63-9211-b6b011cb63a0.webp","thumbnailStoragePath":"
uploads\/thumbnails\/images\/2026\/05\/b33470df-9181-4fd5-be98-1572cd85a0d4.webp","image":true,"
video":false},"logoAsset":{"originalFilename":"stories%2F163%2Fimages%2Fpreview.jpg","width":216,"
height":216,"id":117,"createdAt":1778537076,"fileSizeBytes":3994,"mimeType":"image\/webp","
storagePath":"uploads\/images\/2026\/05\/906eb438-2156-4509-afc9-795e76fc960b.webp","
thumbnailStoragePath":"
uploads\/thumbnails\/images\/2026\/05\/fc6679d5-9e61-44b8-924c-80136b122910.webp","image":true,"
video":false},"menuSound":{"originalFilename":"card_sound_friendzone4.mp3","id":119,"createdAt":
1778537077,"fileSizeBytes":848612,"mimeType":"audio\/mpeg","storagePath":"
uploads\/uploads\/sounds\/2026\/05\/06c47050-5b0b-435e-ab0f-167d79b3bd68.mp3","
thumbnailStoragePath":null,"image":false,"video":false},"createdAt":1778537074,"status":"online","
online":true,"banned":false,"bannedAt":null,"bannedBy":null,"interactionCount":0,"version":1,"
canvasTechnologyRequiredVersion":1,"score":3,"certified":false,"visualTheme":{"id":"campfire"},"
metas":{"id":10,"lang":"fr-FR","title":"Friendzon\u00e9 4","description":"Suite aux \u00e9tranges
apparitions de bugs, une multitudes de retournements de situations vont arriver. Vous \u00eates
toujours \u00e0 la recherche du fameux x92, et vous continuez de faire \u00e9voluer vos relations
avec les personnages du jeu\r\n\r\nVous vous rendrez compte qu'avec de simples mots, bien des
\u00e9motions peuvent faire leur apparition.","catchingPhrase":"La v\u00e9rit\u00e9 est-elle
toujours le meilleur choix \u00e0 faire ?"},"sourceMeta":{"id":10,"lang":"fr-FR","title":"
Friendzon\u00e9 4","description":"Suite aux \u00e9tranges apparitions de bugs, une multitudes de
retournements de situations vont arriver. Vous \u00eates toujours \u00e0 la recherche du fameux x92,
et vous continuez de faire \u00e9voluer vos relations avec les personnages du jeu\r\n\r\nVous vous
rendrez compte qu'avec de simples mots, bien des \u00e9motions peuvent faire leur apparition.","
catchingPhrase":"La v\u00e9rit\u00e9 est-elle toujours le meilleur choix \u00e0 faire ?"},"
userNickNameRequired":true,"official":true,"order":3,"legacyId":163,"free":false,"
searchableArray":{"id":"5JZvpvXaS6r","text":"Friendzon\u00e9 4","image":"
uploads\/thumbnails\/images\/2026\/05\/fc6679d5-9e61-44b8-924c-80136b122910.webp"},"
searchableTitle":"Friendzon\u00e9 4","price":975,"skuIdentifiers":["story_163"],"videoUrl":"http:
\/\/data.sutoko.app\/resources\/preview_full_card_163.mp4","titleAsset":null,"titleAssetLangCode":
null,"author":{"avatarUrl":"
uploads\/thumbnails\/images\/2026\/07\/f919f096-ebfc-4fd7-9467-652f2b9c80fa.webp","displayName":"
hbdeveloppeur"},"chaptersCount":19,"narrativeThemes":[]}