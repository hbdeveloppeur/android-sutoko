package com.purpletear.sutoko.permission.data

import com.purpletear.sutoko.permission.domain.repository.PermissionRepository
import com.purpletear.sutoko.permission.domain.sealed.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionRepositoryImpl() : PermissionRepository {
    private val _permissionRequest: MutableStateFlow<Permission?> = MutableStateFlow(null)
    override val permissionRequest: StateFlow<Permission?>
        get() = _permissionRequest

    override fun requestPermission(permission: Permission) {
        _permissionRequest.value = permission
    }
}