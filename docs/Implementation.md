This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

## Task 1 : Google sent us 3 messages -> [DONE]

Status (2026-08-07, branch `feat/target-sdk-36`):
- Message 1 (target API 35): already satisfied.
- Message 2 (Play Billing >= 8.0.0): already satisfied — `billing = "8.0.0"` in
  `gradle/libs.versions.toml`, resolved as `billing-ktx:8.0.0` in `:platform:play`.
- Message 3 (target API 36): implemented — `compileSdk`/`targetSdk` bumped 35 -> 36
  in all 37 module build files; AGP unified at 8.10.1 (`agp` in libs.versions.toml,
  duplicate `classpath libs.gradle` removed from root buildscript). Validated with
  `./gradlew assembleDebug --no-build-cache` (BUILD SUCCESSFUL) and
  `:platform:play:testDebugUnitTest` (BUILD SUCCESSFUL).

Original messages:
Message 1  : L'appli doit cibler Android 15 (niveau d'API 35) ou une version ultérieure
Pour offrir une expérience sécurisée aux utilisateurs, Google Play exige que toutes les applis
répondent aux exigences du niveau d'API cible.
À compter du 31 août 2025, si votre appli ne cible pas un niveau d'API disponible depuis moins d'un
an après la dernière version d'Android, vous ne serez plus en mesure de mettre à jour votre appli.

Message 2 : L'appli doit utiliser la bibliothèque Google Play Billing 8.0.0 ou version ultérieure
Pour offrir une expérience sécurisée aux utilisateurs, toutes les applis doivent respecter les
exigences de la bibliothèque Google Play Billing.
Votre appli utilise une ancienne version de la bibliothèque Google Play Billing. À compter du 31
août 2026, toutes les applis devront utiliser la version 8.0.0 ou une version ultérieure.

Passez à une version plus récente d'ici cette date pour éviter le refus de vos mises à jour.

Message 3 : L'appli doit cibler Android 16 (niveau d'API 36) ou une version ultérieure
Pour offrir une expérience sécurisée aux utilisateurs, Google Play exige que toutes les applis
répondent aux exigences du niveau d'API cible.
À compter du 31 août 2026, si votre appli ne cible pas un niveau d'API disponible depuis moins d'un
an après la dernière version d'Android, vous ne serez plus en mesure de mettre à jour votre appli./