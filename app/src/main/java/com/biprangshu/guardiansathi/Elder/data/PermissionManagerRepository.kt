package com.biprangshu.guardiansathi.Elder.data

interface PermissionManagerRepository {
    fun isPermissionGranted(permission: String): Boolean
    fun areAllPermissionsGranted(): Boolean
}