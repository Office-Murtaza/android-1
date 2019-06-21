import UIKit

extension UITapGestureRecognizer {
  func didTapAttributedTextInLabel(label: UILabel, inRange targetRange: NSRange) -> Bool {
    let layoutManager = NSLayoutManager()
    let textContainer = NSTextContainer(size: CGSize.zero)
    let textStorage = NSTextStorage(attributedString: label.attributedText!)
    
    layoutManager.addTextContainer(textContainer)
    textStorage.addLayoutManager(layoutManager)
    
    textContainer.lineFragmentPadding = 0.0
    textContainer.lineBreakMode = label.lineBreakMode
    textContainer.maximumNumberOfLines = label.numberOfLines
    let labelSize = label.bounds.size
    textContainer.size = labelSize
    
    // Find the tapped character location and compare it to the specified range
    let textBoundingBox = layoutManager.usedRect(for: textContainer)
    let coefficient = offsetCoefficient(for: label)
    let textContainerOffsetX = (labelSize.width - textBoundingBox.size.width) * coefficient - textBoundingBox.origin.x
    let textContainerOffsetY = (labelSize.height - textBoundingBox.size.height) * coefficient - textBoundingBox.origin.y
    let textContainerOffset = CGPoint(x: textContainerOffsetX, y: textContainerOffsetY)
    
    let locationOfTouchInLabel = self.location(in: label)
    let locationOfTouchInTextContainer = CGPoint(x: locationOfTouchInLabel.x - textContainerOffset.x,
                                                 y: locationOfTouchInLabel.y - textContainerOffset.y)
    
    let indexOfCharacter = layoutManager.characterIndex(for: locationOfTouchInTextContainer,
                                                        in: textContainer,
                                                        fractionOfDistanceBetweenInsertionPoints: nil)
    
    return NSLocationInRange(indexOfCharacter, targetRange)
  }
  
  func offsetCoefficient(for label: UILabel) -> CGFloat {
    switch label.textAlignment {
    case .left, .natural, .justified: return 0.0
    case .right: return 1.0
    case .center: return 0.5
    }
  }
}
