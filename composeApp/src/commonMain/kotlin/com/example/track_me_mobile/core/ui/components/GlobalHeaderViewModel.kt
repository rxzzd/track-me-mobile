package com.example.track_me_mobile.core.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.track_me_mobile.core.domain.models.UserInfo
class GlobalHeaderViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow(Role.UNKNOWN)
    val userRole = _userRole.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // getUserInfo() возвращает Result<UserInfo>
            val result = authRepository.getUserInfo()

            // Пытаемся достать UserInfo, если успех — берем роль, если ошибка — UNKNOWN
            _userRole.value = result.getOrNull()?.mainRole ?: Role.UNKNOWN
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {

            onSuccess()
        }
    }
}