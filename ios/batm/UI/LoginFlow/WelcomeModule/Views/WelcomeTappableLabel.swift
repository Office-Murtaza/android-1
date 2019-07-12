import UIKit
import RxSwift
import RxCocoa

class WelcomeTappableLabel: UILabel {
  
  let dummyButton = DummyButton()
  
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
    
    addSubview(dummyButton)
  }
  
  private func setupLayout() {
    dummyButton.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == WelcomeTappableLabel {
  var tap: Driver<Void> {
    return base.dummyButton.rx.tap.asDriver()
  }
}
