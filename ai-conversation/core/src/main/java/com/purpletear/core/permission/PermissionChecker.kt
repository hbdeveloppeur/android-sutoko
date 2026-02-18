package com.purpletear.core.permission

interface PermissionChecker {

    fun hasStorageWritingPermission(): Boolean
    fun hasMicrophonePermission(): Boolean
}