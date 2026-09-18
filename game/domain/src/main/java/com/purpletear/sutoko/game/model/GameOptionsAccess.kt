package com.purpletear.sutoko.game.model

import com.purpletear.sutoko.domain.model.User

/** Client-side tester access; protected server endpoints must still validate the token. */
fun User?.canAccessGameOptions(): Boolean =
    this?.id == "8be954c7a18f4e7cba9c" && token.isNotBlank()
