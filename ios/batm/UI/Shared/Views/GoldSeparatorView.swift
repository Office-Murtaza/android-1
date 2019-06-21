import UIKit
import RxSwift
import RxCocoa

class GoldSeparatorView: UIView {
  
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
    
    backgroundColor = .lightGold
    layer.cornerRadius = 1.5
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.width.equalTo(26)
      $0.height.equalTo(3)
    }
  }
}
