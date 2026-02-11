package com.aliothmoon.maameow.koin

import com.aliothmoon.maameow.domain.usecase.BuildTaskParamsUseCase
import com.aliothmoon.maameow.domain.service.MaaCompositionService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val useCaseModule = module {
    factory { BuildTaskParamsUseCase(get()) }
}
