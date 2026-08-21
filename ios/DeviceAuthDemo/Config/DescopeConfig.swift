import Foundation

/// Configuration for the Descope project this demo talks to.
///
/// **To run this demo, you only need to set one thing:** paste your Descope
/// Project ID into `projectId` below (and change `baseURL` if your project
/// isn't on the default US region). Everything else is derived from it.
/// See the root README's "Getting Started" section for the full setup
/// walkthrough, including where to find your Project ID and how to enable
/// Device Authentication for your project.
///
/// ## Why `clientId` is just your Project ID
///
/// This demo authenticates as your project's **Generic OIDC Application** —
/// a Federated App every Descope project has by default, and the app type
/// [Descope's Device Authentication docs](https://docs.descope.com/auth-methods/device-auth)
/// describe using for this flow. Per
/// [Descope's OIDC Federated Applications docs](https://docs.descope.com/identity-federation/applications/oidc-apps),
/// its `client_id` is literally your Project ID — that's the documented
/// value, not a shortcut, and it's why there's no separate app to create in
/// the Console.
///
enum DescopeConfig {

    /// Your Descope Project ID — Console → Project Settings.
    /// **Replace this with your own before running the app.**
    static let projectId = "<ProjectId>"

    /// Regional Descope API hosts. Pick the one that matches your project,
    /// or set your own custom domain if you have one configured. See
    /// https://docs.descope.com/management/project-settings/multi-regional
    enum Region: String {
        case us = "https://api.descope.com"
        case eu = "https://api.euc1.descope.com"
        case au = "https://api.aps2.descope.com"
        case ca = "https://api.cac1.descope.com"
    }

    /// The base URL for your Descope project's API. Defaults to the US
    /// region — change to a different `Region` case, or your own custom
    /// domain string, if needed.
    static let baseURL = URL(string: Region.us.rawValue)!

    /// OAuth client ID for the device flow — always the same as
    /// `projectId` for this app type (see the type doc above). You
    /// shouldn't need to change this separately from `projectId`.
    static var clientId: String { projectId }

    /// Space-delimited OAuth scopes requested for the device flow.
    static let scope = "openid profile email"

    /// `true` once `projectId` has been changed from its placeholder.
    /// Used to show a clear setup message instead of a confusing network
    /// error if someone runs the app before configuring it.
    static var isConfigured: Bool { projectId != "<ProjectId>" }

    /// Device authorization ("start") endpoint. Project-scoped — see the
    /// root README for why this differs from the generic path shown in
    /// Descope's docs.
    static var deviceAuthorizationURL: URL {
        baseURL.appendingPathComponent("oauth2/v1/\(projectId)/device")
    }

    /// Token endpoint, polled during the device flow and used for the
    /// `device_code` grant.
    static var tokenURL: URL {
        baseURL.appendingPathComponent("oauth2/v1/\(projectId)/token")
    }
}
