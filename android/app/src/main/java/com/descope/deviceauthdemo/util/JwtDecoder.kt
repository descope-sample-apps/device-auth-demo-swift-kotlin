package com.descope.deviceauthdemo.util

import android.util.Base64
import org.json.JSONObject

/**
 * A tiny, dependency-free helper that decodes the payload of a JWT for
 * *display purposes only*.
 *
 * This intentionally does NOT verify the token's signature, issuer, or
 * expiry — it just base64url-decodes the middle segment so the demo can
 * show who logged in. Do not use this as a substitute for real token
 * verification in a production app (validate signatures against your
 * Descope project's JWKS, e.g. via a backend, before trusting any claim).
 */
object JwtDecoder {

    fun decodeClaims(jwt: String): JSONObject? {
        val segments = jwt.split(".")
        if (segments.size < 2) return null

        return try {
            val payloadBytes = Base64.decode(
                segments[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            JSONObject(String(payloadBytes, Charsets.UTF_8))
        } catch (t: Throwable) {
            null
        }
    }
}
