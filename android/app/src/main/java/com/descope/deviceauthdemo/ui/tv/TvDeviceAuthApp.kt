package com.descope.deviceauthdemo.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.descope.deviceauthdemo.viewmodel.DeviceAuthUiState
import com.descope.deviceauthdemo.viewmodel.DeviceAuthViewModel

/**
 * Android TV / Google TV entry point — same [DeviceAuthViewModel] and state
 * machine as the phone [com.descope.deviceauthdemo.ui.DeviceAuthApp], just
 * routed to the D-pad-focused `tv-material` screens instead.
 */
@Composable
fun TvDeviceAuthApp(modifier: Modifier = Modifier, viewModel: DeviceAuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        is DeviceAuthUiState.SignedOut ->
            TvSignInScreen(isStarting = false, errorMessage = null, onSignIn = viewModel::startSignIn, modifier = modifier)

        is DeviceAuthUiState.StartingFlow ->
            TvSignInScreen(isStarting = true, errorMessage = null, onSignIn = viewModel::startSignIn, modifier = modifier)

        is DeviceAuthUiState.AwaitingApproval ->
            TvVerificationScreen(
                authorization = current.authorization,
                secondsRemaining = current.secondsRemaining,
                onCancel = viewModel::cancelSignIn,
                modifier = modifier
            )

        is DeviceAuthUiState.SignedIn ->
            TvProfileScreen(token = current.token, claims = current.claims, onSignOut = viewModel::signOut, modifier = modifier)

        is DeviceAuthUiState.Error ->
            TvSignInScreen(isStarting = false, errorMessage = current.message, onSignIn = viewModel::startSignIn, modifier = modifier)
    }
}
