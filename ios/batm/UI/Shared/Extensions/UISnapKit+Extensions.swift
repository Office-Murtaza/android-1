import UIKit
import SnapKit

extension ConstraintMaker {
  @discardableResult
  public func keepRatio(for imageView: UIImageView) -> ConstraintMakerEditable {
    return width.equalTo(imageView.snp.height)
      .multipliedBy(imageView.image!.size.width / imageView.image!.size.height)
  }
}
