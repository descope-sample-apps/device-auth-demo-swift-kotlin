package com.descope.deviceauthdemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.descope.deviceauthdemo.model.DeviceTokenResponse
import com.descope.deviceauthdemo.model.IdTokenClaims

@Composable
fun ProfileScreen(
    token: DeviceTokenResponse,
    claims: IdTokenClaims?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // On a tablet, cap the content to a readable column centered in the
    // available space instead of letting it stretch across a much wider
    // screen than a phone.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "✅", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text("Signed in", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            claims?.name?.let { LabeledRow("Name", it) }
            claims?.email?.let { LabeledRow("Email", it) }
            claims?.sub?.let { LabeledRow("Subject", it) }
            LabeledRow("Access token", truncate(token.accessToken))
            token.scope?.takeIf { it.isNotEmpty() }?.let { LabeledRow("Scope", it) }
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
    }
}

private fun truncate(token: String): String {
    if (token.length <= 24) return token
    return "${token.take(12)}…${token.takeLast(8)}"
}
