import SwiftUI

struct VerificationView: View {
    let authorization: DeviceAuthorizationResponse
    let secondsRemaining: Int
    let onCancel: () -> Void

    // On iPad (.regular horizontal size class), cap the content to a
    // readable column instead of letting it stretch edge-to-edge.
    #if os(iOS)
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    private var contentMaxWidth: CGFloat? {
        horizontalSizeClass == .regular ? 500 : nil
    }
    #endif

    // Bigger everything for a 10-foot viewing distance on tvOS.
    #if os(tvOS)
    private let titleFont: Font = .system(size: 44, weight: .bold)
    private let bodyFont: Font = .title3
    private let codeFontSize: CGFloat = 64
    private let qrSize: CGFloat = 320
    private let horizontalPadding: CGFloat = 80
    #else
    private let titleFont: Font = .title2.bold()
    private let bodyFont: Font = .body
    private let codeFontSize: CGFloat = 36
    private let qrSize: CGFloat = 180
    private let horizontalPadding: CGFloat = 16
    #endif

    var body: some View {
        VStack(spacing: 20) {
            Text("Approve this sign-in")
                .font(titleFont)
                .padding(.top, 24)

            Text("Go to")
                .font(bodyFont)
                .foregroundStyle(.secondary)
            Text(authorization.verificationUri)
                .font(.callout.monospaced())
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)

            Text("and enter the code")
                .font(bodyFont)
                .foregroundStyle(.secondary)

            Text(formattedUserCode)
                .font(.system(size: codeFontSize, weight: .bold, design: .monospaced))
                .kerning(2)
                .padding(.vertical, 8)
                .padding(.horizontal, 20)
                .background(RoundedRectangle(cornerRadius: 12).fill(Color.secondary.opacity(0.12)))

            if let complete = authorization.verificationUriComplete {
                VStack(spacing: 8) {
                    Text("or scan this QR code")
                        .font(bodyFont)
                        .foregroundStyle(.secondary)
                    QRCodeView(content: complete)
                        .frame(width: qrSize, height: qrSize)
                }
                .padding(.top, 8)
            }

            Spacer()

            VStack(spacing: 4) {
                ProgressView()
                Text("Waiting for approval… code expires in \(formattedCountdown)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

            Button("Cancel", role: .cancel, action: onCancel)
                .padding(.bottom, 32)
        }
        .padding(.horizontal, horizontalPadding)
        #if os(iOS)
        .frame(maxWidth: contentMaxWidth)
        .frame(maxWidth: .infinity) // re-center the capped column on iPad
        #endif
    }

    private var formattedUserCode: String {
        // Descope returns codes like "WDJB-MJHT" already formatted; fall back
        // to inserting a separator if a provider ever returns one without.
        authorization.userCode
    }

    private var formattedCountdown: String {
        let minutes = secondsRemaining / 60
        let seconds = secondsRemaining % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}

#Preview {
    VerificationView(
        authorization: DeviceAuthorizationResponse(
            deviceCode: "device-code",
            userCode: "WDJB-MJHT",
            verificationUri: "https://auth.example.com/device",
            verificationUriComplete: "https://auth.example.com/device?user_code=WDJB-MJHT",
            expiresIn: 1800,
            interval: 5
        ),
        secondsRemaining: 1234,
        onCancel: {}
    )
}
