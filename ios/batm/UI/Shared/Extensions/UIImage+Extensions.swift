import UIKit

extension UIImage {
  static func qrCode(from string: String) -> UIImage? {
    let data = string.data(using: String.Encoding.ascii)
    
    if let filter = CIFilter(name: "CIQRCodeGenerator") {
      filter.setValue(data, forKey: "inputMessage")
      let transform = CGAffineTransform(scaleX: 3, y: 3)
      
      if let output = filter.outputImage?.transformed(by: transform) {
        return UIImage(ciImage: output)
      }
    }
    
    return nil
  }
  
  static func image(
    color: UIColor,
    size: CGSize = CGSize(width: 1, height: 1),
    cornerRadius radius: CGFloat = 0) -> UIImage {
    
    guard size.width >= radius * 2 && size.height >= radius * 2 else {
      fatalError("Radius is bigger than expected")
    }
    
    let rect = CGRect(origin: .zero, size: size)
    
    UIGraphicsBeginImageContext(rect.size)
    
    guard let context = UIGraphicsGetCurrentContext() else {
      fatalError("Unable to get drawing context")
    }
    
    let path = UIBezierPath(roundedRect: rect, cornerRadius: radius)
    path.addClip()
    color.setFill()
    context.addPath(path.cgPath)
    context.fillPath()
    
    guard let image = UIGraphicsGetImageFromCurrentImageContext() else {
      fatalError("Unable to get image from current drawing context")
    }
    
    UIGraphicsEndImageContext()
    
    return image
  }
}

extension UIImage {
    func withTint(_ hexString: String) -> UIImage {
        if #available(iOS 13.0, *) {
            return withTintColor(UIColor(hexString: hexString))
        } else {
            return self
        }
    }
}
