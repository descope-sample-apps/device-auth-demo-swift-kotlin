import SwiftUI
import CoreImage.CIFilterBuiltins

/// Renders a QR code for the given string using Core Image, no third-party
/// dependency required.
struct QRCodeView: View {
    let content: String

    var body: some View {
        if let image = Self.makeQRImage(from: content) {
            Image(uiImage: image)
                .interpolation(.none)
                .resizable()
                .scaledToFit()
        } else {
            Rectangle()
                .fill(Color.secondary.opacity(0.2))
                .overlay(Text("QR unavailable").font(.caption))
        }
    }

    private static func makeQRImage(from string: String) -> UIImage? {
        let context = CIContext()
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(string.utf8)
        filter.correctionLevel = "M"

        guard let outputImage = filter.outputImage else { return nil }
        let scaled = outputImage.transformed(by: CGAffineTransform(scaleX: 8, y: 8))

        guard let cgImage = context.createCGImage(scaled, from: scaled.extent) else { return nil }
        return UIImage(cgImage: cgImage)
    }
}
