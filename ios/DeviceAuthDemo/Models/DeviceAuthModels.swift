import Foundation

/// Response from `POST /oauth2/v1/device`.
/// See https://docs.descope.com/auth-methods/device-auth
struct DeviceAuthorizationResponse: Decodable {
    let deviceCode: String
    let userCode: String
    let verificationUri: String
    let verificationUriComplete: String?
    let expiresIn: Int
    let interval: Int?

    enum CodingKeys: String, CodingKey {
        case deviceCode = "device_code"
        case userCode = "user_code"
        case verificationUri = "verification_uri"
        case verificationUriComplete = "verification_uri_complete"
        case expiresIn = "expires_in"
        case interval
    }
}

/// Successful response from `POST /oauth2/v1/token`.
struct DeviceTokenResponse: Decodable {
    let accessToken: String
    let tokenType: String
    let refreshToken: String?
    let idToken: String?
    let expiresIn: Int?
    let scope: String?

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case tokenType = "token_type"
        case refreshToken = "refresh_token"
        case idToken = "id_token"
        case expiresIn = "expires_in"
        case scope
    }
}

/// Error body returned by the token endpoint while the user hasn't finished
/// (or has declined) authorizing the device. Per RFC 8628 / Descope docs the
/// `error` field is one of:
///   - "authorization_pending" — keep polling at the current interval
///   - "slow_down"             — increase the polling interval and continue
///   - "access_denied"         — the user declined; stop polling
///   - "expired_token"         — the user_code/device_code expired; stop polling
struct DeviceTokenErrorResponse: Decodable {
    let error: String
    let errorDescription: String?

    enum CodingKeys: String, CodingKey {
        case error
        case errorDescription = "error_description"
    }
}

enum DeviceFlowPollError: String {
    case authorizationPending = "authorization_pending"
    case slowDown = "slow_down"
    case accessDenied = "access_denied"
    case expiredToken = "expired_token"
    case unknown

    init(rawValue value: String) {
        switch value {
        case "authorization_pending": self = .authorizationPending
        case "slow_down": self = .slowDown
        case "access_denied": self = .accessDenied
        case "expired_token": self = .expiredToken
        default: self = .unknown
        }
    }
}

/// Minimal set of decoded ID token claims we show on the profile screen.
/// Note: this demo decodes the JWT payload for *display purposes only* and
/// does not verify the signature. Production apps must verify the token
/// (e.g. against your Descope project's JWKS) before trusting its claims.
struct IDTokenClaims: Decodable {
    let sub: String?
    let email: String?
    let name: String?
    let issuer: String?
    let issuedAt: Double?
    let expiresAt: Double?

    enum CodingKeys: String, CodingKey {
        case sub, email, name
        case issuer = "iss"
        case issuedAt = "iat"
        case expiresAt = "exp"
    }
}
