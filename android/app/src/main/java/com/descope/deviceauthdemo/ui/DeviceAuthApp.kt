package com.descope.deviceauthdemo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.descope.deviceauthdemo.viewmodel.DeviceAuthUiState
import com.descope.deviceauthdemo.viewmodel.DeviceAuthViewModel

@Composable
fun DeviceAuthApp(modifier: Modifier = Modifier, viewModel: DeviceAuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        is DeviceAuthUiState.SignedOut ->
            SignInScreen(isStarting = false, errorMessage = null, onSignIn = viewModel::startSignIn, modifier = modifier)

        is DeviceAuthUiState.StartingFlow ->
            SignInScreen(isStarting = true, errorMessage = null, onSignIn = viewModel::startSignIn, modifier = modifier)

        is DeviceAuthUiState.AwaitingApproval ->
            VerificationScreen(
                authorization = current.authorization,
                secondsRemaining = current.secondsRemaining,
                onCancel = viewModel::cancelSignIn,
                modifier = modifier
            )

        is DeviceAuthUiState.SignedIn ->
            ProfileScreen(token = current.token, claims = current.claims, onSignOut = viewModel::signOut, modifier = modifier)

        is DeviceAuthUiState.Error ->
            SignInScreen(isStarting = false, errorMessage = current.message, onSignIn = viewModel::startSignIn, modifier = modifier)
    }
}
