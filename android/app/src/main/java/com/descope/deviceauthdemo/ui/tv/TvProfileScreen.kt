package com.descope.deviceauthdemo.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.descope.deviceauthdemo.model.DeviceTokenResponse
import com.descope.deviceauthdemo.model.IdTokenClaims

/**
 * 10-foot-UI counterpart to [com.descope.deviceauthdemo.ui.ProfileScreen].
 */
@Composable
fun TvProfileScreen(
    token: DeviceTokenResponse,
    claims: IdTokenClaims?,
    onSignOut: () -> Unit,
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
        Text(text = "✅", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text("Signed in", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            claims?.name?.let { TvLabeledRow("Name", it) }
            claims?.email?.let { TvLabeledRow("Email", it) }
            claims?.sub?.let { TvLabeledRow("Subject", it) }
            TvLabeledRow("Access token", truncate(token.accessToken))
            token.scope?.takeIf { it.isNotEmpty() }?.let { TvLabeledRow("Scope", it) }
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onSignOut, modifier = Modifier.focusRequester(focusRequester)) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun TvLabeledRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
    }
}

private fun truncate(token: String): String {
    if (token.length <= 24) return token
    return "${token.take(12)}…${token.takeLast(8)}"
}
