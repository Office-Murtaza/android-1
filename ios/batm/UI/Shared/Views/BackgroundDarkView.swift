import UIKit
import RxSwift
import RxCocoa

class BackgroundDarkView: UIView {
  
  let dummyButton = UIButton(type: .system)
  
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
    
    backgroundColor = UIColor.blackTwo.withAlphaComponent(0.5)
    
    addSubview(dummyButton)
  }
  
  private func setupLayout() {
    dummyButton.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == BackgroundDarkView {
  var tap: Driver<Void> {
    return base.dummyButton.rx.tap.asDriver()
  }
}
