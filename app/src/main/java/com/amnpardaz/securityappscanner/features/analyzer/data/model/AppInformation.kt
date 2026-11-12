package com.amnpardaz.securityappscanner.features.analyzer.data.model

data class AppInformation(
    val appName: String,
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    val firstInstallTime: Long,
    val requestedPermissions: List<PermissionInformation>,
    val riskLevel: RiskLevel,
    val isSystemApp: Boolean = false,
    val isSideloaded: Boolean = false,
    val isDebuggable: Boolean = false,
    val installSource: String? = null,
    val riskReasons: List<String> = emptyList(),
)
