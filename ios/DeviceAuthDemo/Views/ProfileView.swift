import SwiftUI

struct ProfileView: View {
    let token: DeviceTokenResponse
    let claims: IDTokenClaims?
    let onSignOut: () -> Void

    // On iPad (.regular horizontal size class), cap the content to a
    // readable column instead of letting it stretch edge-to-edge.
    #if os(iOS)
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    private var contentMaxWidth: CGFloat? {
        horizontalSizeClass == .regular ? 500 : nil
    }
    #endif

    #if os(tvOS)
    private let iconSize: CGFloat = 80
    private let titleFont: Font = .system(size: 40, weight: .bold)
    private let labelFont: Font = .title3
    private let valueFont: Font = .title2.monospaced()
    private let horizontalPadding: CGFloat = 80
    #else
    private let iconSize: CGFloat = 56
    private let titleFont: Font = .title2.bold()
    private let labelFont: Font = .caption
    private let valueFont: Font = .callout.monospaced()
    private let horizontalPadding: CGFloat = 16
    #endif

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: iconSize))
                .foregroundStyle(.green)
                .padding(.top, 32)

            Text("Signed in")
                .font(titleFont)

            VStack(alignment: .leading, spacing: 12) {
                if let name = claims?.name {
                    labeledRow(label: "Name", value: name)
                }
                if let email = claims?.email {
                    labeledRow(label: "Email", value: email)
                }
                if let sub = claims?.sub {
                    labeledRow(label: "Subject", value: sub)
                }
                labeledRow(label: "Access token", value: truncated(token.accessToken))
                if let scope = token.scope, !scope.isEmpty {
                    labeledRow(label: "Scope", value: scope)
                }
            }
            .padding()
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.secondary.opacity(0.08)))
            .padding(.horizontal, horizontalPadding)

            Spacer()

            Button("Sign Out", role: .destructive, action: onSignOut)
                .buttonStyle(.bordered)
                .controlSize(.large)
                .padding(.bottom, 40)
        }
        #if os(iOS)
        .frame(maxWidth: contentMaxWidth)
        .frame(maxWidth: .infinity) // re-center the capped column on iPad
        #endif
    }

    private func labeledRow(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(labelFont)
                .foregroundStyle(.secondary)
            Text(value)
                .font(valueFont)
                .lineLimit(2)
                .truncationMode(.middle)
        }
    }

    private func truncated(_ token: String) -> String {
        guard token.count > 24 else { return token }
        let prefix = token.prefix(12)
        let suffix = token.suffix(8)
        return "\(prefix)…\(suffix)"
    }
}

#Preview {
    ProfileView(
        token: DeviceTokenResponse(
            accessToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.example.signature",
            tokenType: "Bearer",
            refreshToken: nil,
            idToken: nil,
            expiresIn: 3600,
            scope: "openid profile email"
        ),
        claims: IDTokenClaims(sub: "user-123", email: "jane@example.com", name: "Jane Doe", issuer: nil, issuedAt: nil, expiresAt: nil),
        onSignOut: {}
    )
}
