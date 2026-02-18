package com.example.track_me_mobile.core.ui.models

data class HeaderMenuItem(
    val text: String,
    val isSelected: Boolean = false,
    val isBold: Boolean = false,
    val action: () -> Unit // Перенесли в самый конец
)