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
  case send
  case verify
  case vipVerify
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
    let title: String
    let textColor: UIColor
    
    switch type {
    case .create:
      backgroundColor = .lightGold
    case .recover:
      backgroundColor = UIColor.greyish.withAlphaComponent(0.15)
    case .cancel:
      backgroundColor = .whiteTwo
    default:
      backgroundColor = .ceruleanBlue
    }
    
    switch type {
    case .cancel, .recover:
      textColor = .slateGrey
    default:
      textColor = .white
    }
    
    switch type {
    case .create:
      title = localize(L.Welcome.CreateButton.title)
    case .recover:
      title = localize(L.Welcome.RecoverButton.title)
    case .cancel:
      title = localize(L.Shared.cancel)
    case .next:
      title = localize(L.Shared.next)
    case .done:
      title = localize(L.Shared.done)
    case .change:
      title = localize(L.Shared.change)
    case .send:
      title = localize(L.Shared.send)
    case .verify:
      title = localize(L.VerificationInfo.Button.verify)
    case .vipVerify:
      title = localize(L.VerificationInfo.Button.vipVerify)
    }
    
    let attributes: [NSAttributedString.Key: Any] = [
      .font: UIFont.poppinsBold14,
      .foregroundColor: textColor
    ]
    let attributedText = NSAttributedString(string: title, attributes: attributes)
    setAttributedTitle(attributedText, for: .normal)
  }
}
