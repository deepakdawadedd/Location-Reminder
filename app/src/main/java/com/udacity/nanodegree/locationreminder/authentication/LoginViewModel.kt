package com.udacity.nanodegree.locationreminder.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map


class LoginViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATE, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) AuthenticationState.AUTHENTICATE
        else AuthenticationState.UNAUTHENTICATED
    }

}