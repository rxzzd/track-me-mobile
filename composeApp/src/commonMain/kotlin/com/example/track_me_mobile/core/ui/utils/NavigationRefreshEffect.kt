package com.example.track_me_mobile.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Запускает [onRefresh] каждый раз когда меняется размер стека навигации.
 * Срабатывает при первом открытии и при каждом возврате назад (pop).
 *
 * Для Screen-компонентов Voyager рекомендуется использовать паттерн isTopScreen напрямую:
 * ```kotlin
 * val isTopScreen = navigator.lastItem == this
 * LaunchedEffect(isTopScreen) {
 *     if (isTopScreen) viewModel.loadData()
 * }
 * ```
 *
 * Этот хелпер подходит для @Composable функций не являющихся Screen:
 * ```kotlin
 * NavigationRefreshEffect { viewModel.loadData() }
 * ```
 */
@Composable
fun NavigationRefreshEffect(onRefresh: () -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    // items.size — надёжный ключ: уменьшается при pop(), увеличивается при push()
    LaunchedEffect(navigator.items.size) {
        currentOnRefresh()
    }
}