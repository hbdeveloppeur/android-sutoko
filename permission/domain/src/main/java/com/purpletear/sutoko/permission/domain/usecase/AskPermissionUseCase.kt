package com.purpletear.sutoko.permission.domain.usecase

import com.purpletear.sutoko.permission.domain.repository.PermissionRepository
import com.purpletear.sutoko.permission.domain.sealed.Permission
import javax.inject.Inject

class AskPermissionUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository,
) {
    operator fun invoke(permission: Permission) {
        return permissionRepository.requestPermission(
            permission = permission,
        )
    }
}