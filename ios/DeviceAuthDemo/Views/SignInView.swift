import SwiftUI

struct SignInView: View {
    let isStarting: Bool
    let errorMessage: String?
    let onSignIn: () -> Void

    // On iPad (.regular horizontal size class), cap the content to a
    // readable column instead of letting it stretch edge-to-edge across a
    // much wider screen than an iPhone. Not meaningful on tvOS, where the
    // #if os(tvOS) values below already size everything for a fixed
    // 10-foot layout.
    #if os(iOS)
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    private var contentMaxWidth: CGFloat? {
        horizontalSizeClass == .regular ? 500 : nil
    }
    #endif

    // tvOS is viewed from ~10 feet away, so type, icon, and padding all scale
    // up substantially compared to the iOS layout.
    #if os(tvOS)
    private let iconSize: CGFloat = 96
    private let titleFont: Font = .system(size: 48, weight: .bold)
    private let bodyFont: Font = .title3
    private let horizontalPadding: CGFloat = 120
    #else
    private let iconSize: CGFloat = 56
    private let titleFont: Font = .title2.bold()
    private let bodyFont: Font = .subheadline
    private let horizontalPadding: CGFloat = 32
    #endif

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "tv.and.mediabox")
                .font(.system(size: iconSize))
                .foregroundStyle(.tint)

            VStack(spacing: 8) {
                Text("Descope Device Flow Demo")
                    .font(titleFont)
                Text("Sign in here the way a smart TV or set-top box would: get a code on this screen, then approve it from your phone or computer.")
                    .font(bodyFont)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, horizontalPadding)
            }

            if let errorMessage {
                Text(errorMessage)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, horizontalPadding)
            }

            Spacer()

            Button(action: onSignIn) {
                if isStarting {
                    ProgressView()
                        .tint(.white)
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Sign In")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(isStarting)
            .padding(.horizontal, horizontalPadding)
            .padding(.bottom, 40)
            #if os(tvOS)
            // SwiftUI buttons are focusable via the Siri Remote/focus engine
            // automatically on tvOS — just keep the tap target a sensible
            // width instead of edge-to-edge like on a phone.
            .frame(maxWidth: 500)
            #endif
        }
        #if os(iOS)
        .frame(maxWidth: contentMaxWidth)
        .frame(maxWidth: .infinity) // re-center the capped column on iPad
        #endif
    }
}

#Preview {
    SignInView(isStarting: false, errorMessage: nil, onSignIn: {})
}
