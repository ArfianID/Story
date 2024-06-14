package com.arfian.story.data.pref

data class SessionModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)