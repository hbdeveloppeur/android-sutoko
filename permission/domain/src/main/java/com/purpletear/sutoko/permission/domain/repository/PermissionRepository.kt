package com.purpletear.sutoko.permission.domain.repository

import com.purpletear.sutoko.permission.domain.sealed.Permission
import kotlinx.coroutines.flow.StateFlow

interface PermissionRepository {
    val permissionRequest: StateFlow<Permission?>

    fun requestPermission(permission: Permission)
}