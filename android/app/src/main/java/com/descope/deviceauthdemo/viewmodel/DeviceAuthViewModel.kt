package com.descope.deviceauthdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descope.deviceauthdemo.model.DeviceAuthorizationResponse
import com.descope.deviceauthdemo.model.DeviceFlowPollError
import com.descope.deviceauthdemo.model.DeviceTokenResponse
import com.descope.deviceauthdemo.model.IdTokenClaims
import com.descope.deviceauthdemo.network.DeviceAuthService
import com.descope.deviceauthdemo.util.JwtDecoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class DeviceAuthUiState {
    data object SignedOut : DeviceAuthUiState()
    data object StartingFlow : DeviceAuthUiState()
    data class AwaitingApproval(
        val authorization: DeviceAuthorizationResponse,
        val secondsRemaining: Int
    ) : DeviceAuthUiState()
    data class SignedIn(val token: DeviceTokenResponse, val claims: IdTokenClaims?) : DeviceAuthUiState()
    data class Error(val message: String) : DeviceAuthUiState()
}

class DeviceAuthViewModel(
    private val service: DeviceAuthService = DeviceAuthService()
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceAuthUiState>(DeviceAuthUiState.SignedOut)
    val state: StateFlow<DeviceAuthUiState> = _state

    private var flowJob: Job? = null

    /**
     * Kicks off the device flow: requests a device/user code pair, shows it
     * to the user, then polls the token endpoint until it resolves.
     */
    fun startSignIn() {
        flowJob?.cancel()
        _state.value = DeviceAuthUiState.StartingFlow

        flowJob = viewModelScope.launch {
            try {
                val authorization = service.startDeviceAuthorization()
                _state.value = DeviceAuthUiState.AwaitingApproval(authorization, authorization.expiresIn)
                pollAndCountdown(authorization)
            } catch (e: Exception) {
                _state.value = DeviceAuthUiState.Error(e.message ?: "Something went wrong starting sign-in.")
            }
        }
    }

    fun signOut() {
        flowJob?.cancel()
        _state.value = DeviceAuthUiState.SignedOut
    }

    /** Call when the user backs out of the "enter this code" screen. */
    fun cancelSignIn() {
        flowJob?.cancel()
        _state.value = DeviceAuthUiState.SignedOut
    }

    private suspend fun pollAndCountdown(authorization: DeviceAuthorizationResponse) {
        var intervalSeconds = (authorization.interval ?: 5).coerceAtLeast(1)
        val deadlineMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(authorization.expiresIn.toLong())
        var lastTickMillis = System.currentTimeMillis()

        while (true) {
            delay(intervalSeconds * 1000L)

            val now = System.currentTimeMillis()
            val elapsedSeconds = ((now - lastTickMillis) / 1000L).toInt()
            lastTickMillis = now

            val remainingMillis = deadlineMillis - now
            if (remainingMillis <= 0) {
                _state.value = DeviceAuthUiState.Error("This code expired before it was approved. Try again.")
                return
            }

            _state.update { current ->
                if (current is DeviceAuthUiState.AwaitingApproval) {
                    current.copy(secondsRemaining = (current.secondsRemaining - elapsedSeconds).coerceAtLeast(0))
                } else current
            }

            when (val result = service.pollForToken(authorization.deviceCode)) {
                is DeviceAuthService.PollResult.Success -> {
                    val claims = result.token.idToken
                        ?.let { JwtDecoder.decodeClaims(it) }
                        ?.let { IdTokenClaims.fromJson(it) }
                    _state.value = DeviceAuthUiState.SignedIn(result.token, claims)
                    return
                }

                is DeviceAuthService.PollResult.Pending -> {
                    if (result.reason == DeviceFlowPollError.SLOW_DOWN) {
                        intervalSeconds += 5
                    }
                    // otherwise authorization_pending: keep polling as-is
                }

                is DeviceAuthService.PollResult.TerminalFailure -> {
                    _state.value = when (result.reason) {
                        DeviceFlowPollError.ACCESS_DENIED ->
                            DeviceAuthUiState.Error("Sign-in was declined on the other device.")
                        DeviceFlowPollError.EXPIRED_TOKEN ->
                            DeviceAuthUiState.Error("This code expired before it was approved. Try again.")
                        else ->
                            DeviceAuthUiState.Error("Sign-in could not be completed.")
                    }
                    return
                }
            }
        }
    }
}
