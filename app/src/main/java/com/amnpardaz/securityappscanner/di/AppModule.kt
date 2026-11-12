package com.amnpardaz.securityappscanner.di

import com.amnpardaz.securityappscanner.features.analyzer.domain.viewModel.AnalyzerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::AnalyzerViewModel)
}
