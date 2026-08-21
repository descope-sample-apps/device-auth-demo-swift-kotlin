import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = DeviceAuthViewModel()

    var body: some View {
        Group {
            switch viewModel.state {
            case .signedOut:
                SignInView(isStarting: false, errorMessage: nil, onSignIn: viewModel.startSignIn)

            case .startingFlow:
                SignInView(isStarting: true, errorMessage: nil, onSignIn: viewModel.startSignIn)

            case .awaitingApproval(let authorization, let secondsRemaining):
                VerificationView(
                    authorization: authorization,
                    secondsRemaining: secondsRemaining,
                    onCancel: viewModel.cancelSignIn
                )

            case .signedIn(let token, let claims):
                ProfileView(token: token, claims: claims, onSignOut: viewModel.signOut)

            case .error(let message):
                SignInView(isStarting: false, errorMessage: message, onSignIn: viewModel.startSignIn)
            }
        }
        .animation(.default, value: isCurrentStateTag)
    }

    // Cheap identity for the switch above so SwiftUI animates state changes.
    private var isCurrentStateTag: Int {
        switch viewModel.state {
        case .signedOut: return 0
        case .startingFlow: return 1
        case .awaitingApproval: return 2
        case .signedIn: return 3
        case .error: return 4
        }
    }
}

#Preview {
    ContentView()
}
