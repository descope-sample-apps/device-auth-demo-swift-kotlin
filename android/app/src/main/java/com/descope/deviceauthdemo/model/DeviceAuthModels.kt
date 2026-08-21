package com.descope.deviceauthdemo.model

import org.json.JSONObject

/**
 * Response from `POST /oauth2/v1/device`.
 * See https://docs.descope.com/auth-methods/device-auth
 */
data class DeviceAuthorizationResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val verificationUriComplete: String?,
    val expiresIn: Int,
    val interval: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): DeviceAuthorizationResponse = DeviceAuthorizationResponse(
            deviceCode = json.getString("device_code"),
            userCode = json.getString("user_code"),
            verificationUri = json.getString("verification_uri"),
            verificationUriComplete = json.optStringOrNull("verification_uri_complete"),
            expiresIn = json.getInt("expires_in"),
            interval = if (json.has("interval")) json.getInt("interval") else null
        )
    }
}

/** Successful response from `POST /oauth2/v1/token`. */
data class DeviceTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Int?,
    val scope: String?
) {
    companion object {
        fun fromJson(json: JSONObject): DeviceTokenResponse = DeviceTokenResponse(
            accessToken = json.getString("access_token"),
            tokenType = json.optString("token_type", "Bearer"),
            refreshToken = json.optStringOrNull("refresh_token"),
            idToken = json.optStringOrNull("id_token"),
            expiresIn = if (json.has("expires_in")) json.getInt("expires_in") else null,
            scope = json.optStringOrNull("scope")
        )
    }
}

/**
 * Error body returned by the token endpoint while the user hasn't finished
 * (or has declined) authorizing the device. Per RFC 8628 / Descope docs the
 * `error` field is one of:
 *  - "authorization_pending" — keep polling at the current interval
 *  - "slow_down"             — increase the polling interval and continue
 *  - "access_denied"         — the user declined; stop polling
 *  - "expired_token"         — the user_code/device_code expired; stop polling
 */
enum class DeviceFlowPollError(val wireValue: String) {
    AUTHORIZATION_PENDING("authorization_pending"),
    SLOW_DOWN("slow_down"),
    ACCESS_DENIED("access_denied"),
    EXPIRED_TOKEN("expired_token"),
    UNKNOWN("");

    companion object {
        fun from(value: String?): DeviceFlowPollError =
            values().firstOrNull { it.wireValue == value } ?: UNKNOWN
    }
}

/**
 * Minimal set of decoded ID token claims we show on the profile screen.
 *
 * Note: this demo decodes the JWT payload for *display purposes only* and
 * does not verify the signature. Production apps must verify the token
 * (e.g. against your Descope project's JWKS) before trusting its claims.
 */
data class IdTokenClaims(
    val sub: String?,
    val email: String?,
    val name: String?
) {
    companion object {
        fun fromJson(json: JSONObject): IdTokenClaims = IdTokenClaims(
            sub = json.optStringOrNull("sub"),
            email = json.optStringOrNull("email"),
            name = json.optStringOrNull("name")
        )
    }
}

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
