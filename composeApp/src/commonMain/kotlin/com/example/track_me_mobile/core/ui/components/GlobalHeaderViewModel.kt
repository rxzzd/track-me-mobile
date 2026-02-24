package com.example.track_me_mobile.core.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GlobalHeaderViewModel(
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _userRole = MutableStateFlow(Role.UNKNOWN)
    val userRole = _userRole.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val result = authRepository.getUserInfo()
            _userRole.value = result.getOrNull()?.mainRole ?: Role.UNKNOWN
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            println("### LOGOUT: Clearing session...")
            sessionStorage.clear()
            _userRole.value = Role.UNKNOWN
            println("### LOGOUT: Session cleared, navigating to login")
            onSuccess()
        }
    }
}