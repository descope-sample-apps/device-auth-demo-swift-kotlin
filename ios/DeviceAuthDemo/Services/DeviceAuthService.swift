import Foundation

/// Talks to Descope's OAuth 2.0 Device Authorization Grant endpoints.
/// Reference: https://docs.descope.com/auth-methods/device-auth
struct DeviceAuthService {

    enum ServiceError: Error, LocalizedError {
        case notConfigured
        case transport(Error)
        case unexpectedStatus(Int, Data)
        case decoding(Error)

        var errorDescription: String? {
            switch self {
            case .notConfigured:
                return "Set your Descope Project ID in DescopeConfig.swift before running this demo."
            case .transport(let error):
                return "Network error: \(error.localizedDescription)"
            case .unexpectedStatus(let status, let data):
                if let descopeError = try? JSONDecoder().decode(DescopeErrorBody.self, from: data),
                   let message = descopeError.message ?? descopeError.errorMessage ?? descopeError.errorDescription {
                    return "\(message) (status \(status))"
                }
                return "Unexpected server response (status \(status))"
            case .decoding:
                return "Couldn't parse the server's response"
            }
        }
    }

    /// Descope's general error response shape (distinct from the RFC 8628
    /// `{error, error_description}` shape used specifically by the token
    /// endpoint's pending/terminal states).
    private struct DescopeErrorBody: Decodable {
        let errorCode: String?
        let errorDescription: String?
        let errorMessage: String?
        let message: String?
    }

    /// The outcome of a single poll against the token endpoint.
    enum PollResult {
        case success(DeviceTokenResponse)
        case pending(DeviceFlowPollError) // authorization_pending or slow_down
        case terminalFailure(DeviceFlowPollError) // access_denied or expired_token
    }

    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    /// Step 1: request a `device_code` / `user_code` pair.
    func startDeviceAuthorization() async throws -> DeviceAuthorizationResponse {
        guard DescopeConfig.isConfigured else {
            throw ServiceError.notConfigured
        }

        var request = URLRequest(url: DescopeConfig.deviceAuthorizationURL)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let bodyParams = [
            "client_id": DescopeConfig.clientId,
            "scope": DescopeConfig.scope
        ]
        request.httpBody = formEncode(bodyParams)

        let (data, response) = try await perform(request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            let status = (response as? HTTPURLResponse)?.statusCode ?? -1
            throw ServiceError.unexpectedStatus(status, data)
        }

        do {
            return try JSONDecoder().decode(DeviceAuthorizationResponse.self, from: data)
        } catch {
            throw ServiceError.decoding(error)
        }
    }

    /// Step 2: poll the token endpoint with the `device_code` grant.
    /// Call this on an interval (see `DeviceAuthorizationResponse.interval`)
    /// until it returns `.success` or a `.terminalFailure`.
    func pollForToken(deviceCode: String) async throws -> PollResult {
        var request = URLRequest(url: DescopeConfig.tokenURL)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let bodyParams = [
            "grant_type": "urn:ietf:params:oauth:grant-type:device_code",
            "device_code": deviceCode,
            "client_id": DescopeConfig.clientId
        ]
        request.httpBody = formEncode(bodyParams)

        let (data, response) = try await perform(request)
        let status = (response as? HTTPURLResponse)?.statusCode ?? -1

        if (200...299).contains(status) {
            do {
                let token = try JSONDecoder().decode(DeviceTokenResponse.self, from: data)
                return .success(token)
            } catch {
                throw ServiceError.decoding(error)
            }
        }

        // Non-2xx: expect an RFC 8628 error body naming what to do next.
        guard let errorBody = try? JSONDecoder().decode(DeviceTokenErrorResponse.self, from: data) else {
            throw ServiceError.unexpectedStatus(status, data)
        }

        let pollError = DeviceFlowPollError(rawValue: errorBody.error)
        switch pollError {
        case .authorizationPending, .slowDown:
            return .pending(pollError)
        case .accessDenied, .expiredToken, .unknown:
            return .terminalFailure(pollError)
        }
    }

    private func perform(_ request: URLRequest) async throws -> (Data, URLResponse) {
        do {
            return try await session.data(for: request)
        } catch {
            throw ServiceError.transport(error)
        }
    }

    private func formEncode(_ params: [String: String]) -> Data {
        let pairs = params.map { key, value -> String in
            let allowed = CharacterSet.urlQueryAllowed.subtracting(CharacterSet(charactersIn: "+&="))
            let encodedKey = key.addingPercentEncoding(withAllowedCharacters: allowed) ?? key
            let encodedValue = value.addingPercentEncoding(withAllowedCharacters: allowed) ?? value
            return "\(encodedKey)=\(encodedValue)"
        }
        return pairs.joined(separator: "&").data(using: .utf8) ?? Data()
    }
}
