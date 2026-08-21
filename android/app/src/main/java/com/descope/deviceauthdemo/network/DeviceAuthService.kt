package com.descope.deviceauthdemo.network

import com.descope.deviceauthdemo.Config
import com.descope.deviceauthdemo.model.DeviceAuthorizationResponse
import com.descope.deviceauthdemo.model.DeviceFlowPollError
import com.descope.deviceauthdemo.model.DeviceTokenResponse
import com.descope.deviceauthdemo.model.optStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

/**
 * Talks to Descope's OAuth 2.0 Device Authorization Grant endpoints.
 * Reference: https://docs.descope.com/auth-methods/device-auth
 */
class DeviceAuthService(
    private val client: OkHttpClient = OkHttpClient()
) {

    /** The outcome of a single poll against the token endpoint. */
    sealed class PollResult {
        data class Success(val token: DeviceTokenResponse) : PollResult()
        data class Pending(val reason: DeviceFlowPollError) : PollResult() // authorization_pending or slow_down
        data class TerminalFailure(val reason: DeviceFlowPollError) : PollResult() // access_denied or expired_token
    }

    class DeviceAuthException(message: String) : IOException(message)

    /** Step 1: request a `device_code` / `user_code` pair. */
    suspend fun startDeviceAuthorization(): DeviceAuthorizationResponse = withContext(Dispatchers.IO) {
        if (!Config.isConfigured) {
            throw DeviceAuthException("Set your Descope Project ID in Config.kt before running this demo.")
        }

        val body = FormBody.Builder()
            .add("client_id", Config.clientId)
            .add("scope", Config.scope)
            .build()

        val request = Request.Builder()
            .url(Config.deviceAuthorizationUrl)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw DeviceAuthException(describeError(response.code, responseBody))
            }
            DeviceAuthorizationResponse.fromJson(JSONObject(responseBody))
        }
    }

    /** Descope's general error response shape, e.g. `{"errorCode", "errorDescription", "errorMessage", "message"}`. */
    private fun describeError(status: Int, responseBody: String): String {
        val message = try {
            val json = JSONObject(responseBody)
            json.optStringOrNull("message")
                ?: json.optStringOrNull("errorMessage")
                ?: json.optStringOrNull("errorDescription")
        } catch (e: Exception) {
            null
        }
        return if (message != null) "$message (status $status)" else "Unexpected server response (status $status)"
    }

    /**
     * Step 2: poll the token endpoint with the `device_code` grant.
     * Call this on an interval (see [DeviceAuthorizationResponse.interval])
     * until it returns [PollResult.Success] or a [PollResult.TerminalFailure].
     */
    suspend fun pollForToken(deviceCode: String): PollResult = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            .add("device_code", deviceCode)
            .add("client_id", Config.clientId)
            .build()

        val request = Request.Builder()
            .url(Config.tokenUrl)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            val json = try {
                JSONObject(responseBody)
            } catch (e: Exception) {
                null
            }

            if (response.isSuccessful && json != null) {
                return@use PollResult.Success(DeviceTokenResponse.fromJson(json))
            }

            val errorCode = json?.optString("error")
            val pollError = DeviceFlowPollError.from(errorCode)
            return@use when (pollError) {
                DeviceFlowPollError.AUTHORIZATION_PENDING,
                DeviceFlowPollError.SLOW_DOWN -> PollResult.Pending(pollError)

                DeviceFlowPollError.ACCESS_DENIED,
                DeviceFlowPollError.EXPIRED_TOKEN,
                DeviceFlowPollError.UNKNOWN -> {
                    if (pollError == DeviceFlowPollError.UNKNOWN && json == null) {
                        throw DeviceAuthException(describeError(response.code, responseBody))
                    }
                    PollResult.TerminalFailure(pollError)
                }
            }
        }
    }
}
