package com.amnpardaz.securityappscanner.features.analyzer.domain.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amnpardaz.securityappscanner.features.analyzer.data.model.AppInformation
import com.amnpardaz.securityappscanner.features.analyzer.data.model.PermissionInformation
import com.amnpardaz.securityappscanner.features.analyzer.data.model.RiskLevel
import com.amnpardaz.securityappscanner.features.analyzer.domain.service.PermissionRiskAnalyzer
import com.amnpardaz.securityappscanner.share.uiUtils.AppsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<AppsUiState>(AppsUiState.Initial)
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private val packageManager = application.packageManager

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = AppsUiState.Loading

            try {
                val apps = withContext(Dispatchers.IO) {
                    getInstalledApps()
                }

                _uiState.value = AppsUiState.Success(apps)

            } catch (e: Exception) {
                _uiState.value = AppsUiState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

    private fun getInstalledApps(): List<AppInformation> {
        val packages = packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS
        )

        return packages.mapNotNull { packageInfo ->
            try {
                val app = packageInfo.applicationInfo
                    ?: return@mapNotNull null

                val permissions = packageInfo.requestedPermissions
                    ?.map { permission ->
                        PermissionRiskAnalyzer.analyze(permission)
                    } ?: emptyList()

                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }

                val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                val isDebuggable = (app.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

                val installSource = getInstallerSource(packageInfo.packageName)
                val isSideloaded = !isSystemApp && (installSource == null || installSource != "com.android.vending")

                val riskReasons = evaluateRiskReasons(
                    permissions = permissions,
                    isDebuggable = isDebuggable,
                    isSideloaded = isSideloaded
                )

                val riskLevel = calculateAppRisk(
                    permissions = permissions,
                    riskReasons = riskReasons
                )

                AppInformation(
                    appName = packageManager.getApplicationLabel(app).toString(),
                    packageName = packageInfo.packageName,
                    versionName = packageInfo.versionName,
                    versionCode = versionCode,
                    firstInstallTime = packageInfo.firstInstallTime,
                    requestedPermissions = permissions,
                    riskLevel = riskLevel,
                    isSystemApp = isSystemApp,
                    isSideloaded = isSideloaded,
                    isDebuggable = isDebuggable,
                    installSource = installSource,
                    riskReasons = riskReasons
                )

            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getInstallerSource(packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun evaluateRiskReasons(
        permissions: List<PermissionInformation>,
        isDebuggable: Boolean,
        isSideloaded: Boolean
    ): List<String> {
        val reasons = mutableListOf<String>()

        val permNames = permissions.map { it.name }.toSet()

        if (isDebuggable) {
            reasons.add("Debuggable build installed on device")
        }

        if (isSideloaded) {
            reasons.add("Sideloaded from unknown source (Not Google Play)")
        }

        if (Manifest.permission.SYSTEM_ALERT_WINDOW in permNames) {
            reasons.add("Overlay permission requested (System Alert Window)")
        }

        if (Manifest.permission.BIND_ACCESSIBILITY_SERVICE in permNames) {
            reasons.add("Accessibility service control requested")
        }

        if (Manifest.permission.REQUEST_INSTALL_PACKAGES in permNames) {
            reasons.add("Package installation permission requested")
        }

        val hasInternet = Manifest.permission.INTERNET in permNames
        val hasContactsOrSms = Manifest.permission.READ_CONTACTS in permNames ||
                Manifest.permission.READ_SMS in permNames ||
                Manifest.permission.READ_CALL_LOG in permNames

        if (hasInternet && hasContactsOrSms) {
            reasons.add("Exfiltration Risk: Internet + Private Data access (Contacts/SMS/Calls)")
        }

        val hasCameraOrMic = Manifest.permission.CAMERA in permNames ||
                Manifest.permission.RECORD_AUDIO in permNames

        if (hasInternet && hasCameraOrMic) {
            reasons.add("Surveillance Risk: Internet + Camera/Microphone access")
        }

        return reasons
    }

    private fun calculateAppRisk(
        permissions: List<PermissionInformation>,
        riskReasons: List<String>
    ): RiskLevel {
        val highCount = permissions.count { it.riskLevel == RiskLevel.HIGH }
        val mediumCount = permissions.count { it.riskLevel == RiskLevel.MEDIUM }

        val hasCriticalReason = riskReasons.any {
            it.contains("Exfiltration Risk") ||
                    it.contains("Surveillance Risk") ||
                    it.contains("Accessibility service") ||
                    it.contains("Overlay permission")
        }

        return when {
            hasCriticalReason || highCount >= 3 -> RiskLevel.HIGH
            highCount >= 1 || riskReasons.isNotEmpty() || mediumCount >= 3 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
}
