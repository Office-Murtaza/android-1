import UIKit
import RxSwift
import RxCocoa

class Divider: UIView {
  
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
    
    backgroundColor = .black10
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.height.equalTo(0.5)
    }
  }
}
