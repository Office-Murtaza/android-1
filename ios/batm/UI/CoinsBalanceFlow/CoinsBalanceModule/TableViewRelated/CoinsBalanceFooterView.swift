import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinsBalanceFooterView: UIView {
  
  let divider = UIView()
  
  let manageWalletsButton = MDCButton.manageWallets
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    backgroundColor = .white
    
    addSubviews(divider,
                manageWalletsButton)
  }
  
  private func setupLayout() {
    divider.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(1 / UIScreen.main.scale)
    }
    manageWalletsButton.snp.makeConstraints {
      $0.top.equalTo(divider.snp.bottom).offset(30)
      $0.bottom.equalToSuperview().inset(30)
      $0.left.right.equalToSuperview().inset(15)
      $0.height.equalTo(50)
    }
  }
}

extension Reactive where Base == CoinsBalanceFooterView {
  var manageWallets: Driver<Void> {
    return base.manageWalletsButton.rx.tap.asDriver()
  }
}
