import UIKit
import RxSwift
import RxCocoa

enum MainButtonType {
  case create
  case recover
  case cancel
  case next
  case done
  case change
  case unlink
}

class MainButton: UIButton {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    layer.cornerRadius = 16
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.height.equalTo(50)
    }
  }
  
  func configure(for type: MainButtonType) {
    let commonAttributes: [NSAttributedString.Key: Any] = [.font: UIFont.poppinsBold14]
    let customAttributes: [NSAttributedString.Key: Any]
    let title: String
    
    switch type {
    case .create:
      backgroundColor = .lightGold
      customAttributes = [.foregroundColor: UIColor.white]
      title = localize(L.Welcome.CreateButton.title)
    case .recover:
      backgroundColor = UIColor.greyish.withAlphaComponent(0.15)
      customAttributes = [.foregroundColor: UIColor.slateGrey]
      title = localize(L.Welcome.RecoverButton.title)
    case .cancel:
      backgroundColor = .whiteTwo
      customAttributes = [.foregroundColor: UIColor.slateGrey]
      title = localize(L.Shared.cancel)
    case .next:
      backgroundColor = .ceruleanBlue
      customAttributes = [.foregroundColor: UIColor.white]
      title = localize(L.Shared.next)
    case .done:
      backgroundColor = .ceruleanBlue
      customAttributes = [.foregroundColor: UIColor.white]
      title = localize(L.Shared.done)
    case .change:
      backgroundColor = .whiteTwo
      customAttributes = [.foregroundColor: UIColor.slateGrey]
      title = localize(L.Shared.change)
    case .unlink:
      backgroundColor = .whiteTwo
      customAttributes = [.foregroundColor: UIColor.slateGrey]
      title = localize(L.Unlink.Button.title)
    }
    
    let allAttributes = commonAttributes.merging(customAttributes, uniquingKeysWith: { _, new in new })
    let attributedText = NSAttributedString(string: title, attributes: allAttributes)
    setAttributedTitle(attributedText, for: .normal)
  }
}
