package com.example.track_me_mobile.features.auth.domain

sealed class AuthError : Throwable() {
    object NetworkError : AuthError()
    object InvalidCredentials : AuthError()
    object ServerError : AuthError()
    data class Unknown(val msg: String) : AuthError()
}