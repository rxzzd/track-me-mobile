package com.example.track_me_mobile.core.di

import org.koin.dsl.module

// Здесь мы будем регистрировать все наши классы
val appModule = module {
    // Пока пусто. Потом тут будет что-то вроде:
    // single { AuthRepositoryImpl() }оке
    // factory { AuthViewModel(get()) }
}