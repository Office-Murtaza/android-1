import UIKit

extension UILabel {
  var actualFontSize: CGFloat {
    let labelContext = NSStringDrawingContext()
    labelContext.minimumScaleFactor = minimumScaleFactor
    
    let attributedString = NSAttributedString(string: text ?? "",
                                              attributes: [.font: font!])
    attributedString.boundingRect(with: frame.size,
                                  options: [.usesLineFragmentOrigin, .usesFontLeading],
                                  context: labelContext)
    
    return font.pointSize * labelContext.actualScaleFactor
  }
}
