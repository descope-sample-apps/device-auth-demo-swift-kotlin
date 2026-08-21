package com.descope.deviceauthdemo.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.descope.deviceauthdemo.model.DeviceAuthorizationResponse
import com.descope.deviceauthdemo.ui.QrCode

/**
 * 10-foot-UI counterpart to
 * [com.descope.deviceauthdemo.ui.VerificationScreen] — bigger code and QR
 * for a couch-distance viewing, D-pad-focusable Cancel button.
 */
@Composable
fun TvVerificationScreen(
    authorization: DeviceAuthorizationResponse,
    secondsRemaining: Int,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 96.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Approve this sign-in", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        Text("Go to", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = authorization.verificationUri,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text("and enter the code", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        Text(
            text = authorization.userCode,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 12.dp)
        )

        if (authorization.verificationUriComplete != null) {
            Spacer(Modifier.height(32.dp))
            Text("or scan this QR code", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            QrCode(
                content = authorization.verificationUriComplete,
                modifier = Modifier.size(280.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        CircularProgressIndicator(modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Waiting for approval… code expires in ${formatCountdown(secondsRemaining)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.focusRequester(focusRequester)) {
            Text("Cancel")
        }
    }
}

private fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
