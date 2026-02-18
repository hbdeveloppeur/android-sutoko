package com.purpletear.sutoko.permission.domain.sealed

sealed class Permission {
    data object Notification : Permission()
}