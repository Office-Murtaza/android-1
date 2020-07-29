import UIKit

extension CALayer {
  func startBlinking(duration: CFTimeInterval = 0.4) {
    let key = "opacity"
    let animation = CABasicAnimation(keyPath: key)
    animation.fromValue = 1.0
    animation.toValue = 0.0
    animation.duration = duration
    animation.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.linear)
    animation.autoreverses = true
    animation.repeatCount = Float.greatestFiniteMagnitude
    add(animation, forKey: key)
  }
  
  func stopBlinking() {
    removeAnimation(forKey: "opacity")
  }
  
  func drawTopShadow(radius: CGFloat = 3.0, height: CGFloat = 1.0, color: UIColor = .black) {
    shadowColor = color.cgColor
    shadowRadius = radius
    shadowOpacity = 1.0
    
    let path = CGMutablePath()
    
    path.move(to: CGPoint.zero)
    path.addLine(to: CGPoint(x: bounds.maxX, y: 0))
    path.addLine(to: CGPoint(x: bounds.maxX, y: height))
    path.addLine(to: CGPoint(x: 0, y: height))
    path.addLine(to: CGPoint.zero)
    
    shadowPath = path
  }
}
