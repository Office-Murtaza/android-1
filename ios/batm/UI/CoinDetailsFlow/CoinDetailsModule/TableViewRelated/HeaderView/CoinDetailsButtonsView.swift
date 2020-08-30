import UIKit
import RxSwift
import RxCocoa

class CoinDetailsButtonsView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.distribution = .fillEqually
    stackView.spacing = 10
    return stackView
  }()
  
  var defaultAttributes: [NSAttributedString.Key: Any] {
    return [.foregroundColor: UIColor.white,
            .font: UIFont.poppinsSemibold12]
  }
  
  var defaultButton: UIButton {
    let button = UIButton()
    button.backgroundColor = .ceruleanBlue
    button.layer.cornerRadius = 8
    return button
  }
  
  lazy var depositButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.deposit), attributes: defaultAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var withdrawButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.withdraw), attributes: defaultAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var sendGiftButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.sendGift), attributes: defaultAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
  lazy var sellButton: UIButton = {
    let button = defaultButton
    let attributedTitle = NSAttributedString(string: localize(L.CoinDetails.sell), attributes: defaultAttributes)
    button.setAttributedTitle(attributedTitle, for: .normal)
    return button
  }()
  
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
    
    addSubviews(stackView)
    stackView.addArrangedSubviews(depositButton,
                                  withdrawButton,
                                  sendGiftButton,
                                  sellButton)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    [depositButton,
     withdrawButton,
     sendGiftButton,
     sellButton].forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(30)
      }
    }
  }
}

extension Reactive where Base == CoinDetailsButtonsView {
  var depositTap: Driver<Void> {
    return base.depositButton.rx.tap.asDriver()
  }
  
  var withdrawTap: Driver<Void> {
    return base.withdrawButton.rx.tap.asDriver()
  }
  
  var sendGiftTap: Driver<Void> {
    return base.sendGiftButton.rx.tap.asDriver()
  }
  
  var sellTap: Driver<Void> {
    return base.sellButton.rx.tap.asDriver()
  }
}

