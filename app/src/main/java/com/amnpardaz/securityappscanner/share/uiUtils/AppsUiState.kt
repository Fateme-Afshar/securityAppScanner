package com.amnpardaz.securityappscanner.share.uiUtils
import com.amnpardaz.securityappscanner.features.analyzer.data.model.AppInformation

sealed interface AppsUiState {

    object Initial : AppsUiState

    object Loading : AppsUiState

    data class Success(
        val apps: List<AppInformation>
    ) : AppsUiState

    data class Error(
        val message: String
    ) : AppsUiState
}
