import Foundation

/// A tiny, dependency-free helper that decodes the payload of a JWT for
/// *display purposes only*.
///
/// This intentionally does NOT verify the token's signature, issuer, or
/// expiry — it just base64url-decodes the middle segment so the demo can
/// show who logged in. Do not use this as a substitute for real token
/// verification in a production app (validate signatures against your
/// Descope project's JWKS, e.g. via a backend, before trusting any claim).
enum JWTDecoder {
    enum DecodeError: Error {
        case malformedToken
    }

    static func decodeClaims<T: Decodable>(from jwt: String, as type: T.Type) throws -> T {
        let segments = jwt.split(separator: ".")
        guard segments.count >= 2 else { throw DecodeError.malformedToken }

        let payloadSegment = String(segments[1])
        guard let payloadData = base64URLDecode(payloadSegment) else {
            throw DecodeError.malformedToken
        }

        return try JSONDecoder().decode(T.self, from: payloadData)
    }

    private static func base64URLDecode(_ value: String) -> Data? {
        var base64 = value
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")

        let paddingLength = 4 - (base64.count % 4)
        if paddingLength < 4 {
            base64 += String(repeating: "=", count: paddingLength)
        }

        return Data(base64Encoded: base64)
    }
}
