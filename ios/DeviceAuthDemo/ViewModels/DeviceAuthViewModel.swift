import Foundation

@MainActor
final class DeviceAuthViewModel: ObservableObject {

    enum State {
        case signedOut
        case startingFlow
        case awaitingApproval(DeviceAuthorizationResponse, secondsRemaining: Int)
        case signedIn(DeviceTokenResponse, IDTokenClaims?)
        case error(String)
    }

    @Published private(set) var state: State = .signedOut

    private let service: DeviceAuthService
    private var pollingTask: Task<Void, Never>?
    private var countdownTask: Task<Void, Never>?

    init(service: DeviceAuthService = DeviceAuthService()) {
        self.service = service
    }

    /// Kicks off the device flow: requests a device/user code pair, shows it
    /// to the user, then polls the token endpoint until it resolves.
    func startSignIn() {
        cancelInFlightWork()
        state = .startingFlow

        pollingTask = Task {
            do {
                let authorization = try await service.startDeviceAuthorization()
                guard !Task.isCancelled else { return }

                let expiry = authorization.expiresIn
                state = .awaitingApproval(authorization, secondsRemaining: expiry)
                startCountdown(totalSeconds: expiry)
                await poll(authorization)
            } catch {
                if !Task.isCancelled {
                    state = .error(error.localizedDescription)
                }
            }
        }
    }

    func signOut() {
        cancelInFlightWork()
        state = .signedOut
    }

    /// Call when the user backs out of the "enter this code" screen.
    func cancelSignIn() {
        cancelInFlightWork()
        state = .signedOut
    }

    // MARK: - Polling

    private func poll(_ authorization: DeviceAuthorizationResponse) async {
        var intervalSeconds = max(authorization.interval ?? 5, 1)
        let deadline = Date().addingTimeInterval(TimeInterval(authorization.expiresIn))

        while !Task.isCancelled {
            if Date() >= deadline {
                state = .error("This code expired before it was approved. Try again.")
                return
            }

            do {
                try await Task.sleep(nanoseconds: UInt64(intervalSeconds) * 1_000_000_000)
            } catch {
                return // cancelled
            }
            if Task.isCancelled { return }

            do {
                let result = try await service.pollForToken(deviceCode: authorization.deviceCode)
                switch result {
                case .success(let token):
                    countdownTask?.cancel()
                    let claims = token.idToken.flatMap {
                        try? JWTDecoder.decodeClaims(from: $0, as: IDTokenClaims.self)
                    }
                    state = .signedIn(token, claims)
                    return

                case .pending(let reason):
                    if reason == .slowDown {
                        intervalSeconds += 5
                    }
                    // otherwise authorization_pending: keep polling as-is

                case .terminalFailure(let reason):
                    countdownTask?.cancel()
                    switch reason {
                    case .accessDenied:
                        state = .error("Sign-in was declined on the other device.")
                    case .expiredToken:
                        state = .error("This code expired before it was approved. Try again.")
                    default:
                        state = .error("Sign-in could not be completed.")
                    }
                    return
                }
            } catch {
                if !Task.isCancelled {
                    state = .error(error.localizedDescription)
                }
                return
            }
        }
    }

    private func startCountdown(totalSeconds: Int) {
        countdownTask?.cancel()
        countdownTask = Task {
            var remaining = totalSeconds
            while !Task.isCancelled && remaining > 0 {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                if Task.isCancelled { return }
                remaining -= 1
                if case .awaitingApproval(let authorization, _) = state {
                    state = .awaitingApproval(authorization, secondsRemaining: remaining)
                }
            }
        }
    }

    private func cancelInFlightWork() {
        pollingTask?.cancel()
        countdownTask?.cancel()
        pollingTask = nil
        countdownTask = nil
    }
}
