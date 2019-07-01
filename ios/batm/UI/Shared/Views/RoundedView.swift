import UIKit
import RxSwift
import RxCocoa

class RoundedView: UIView {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    backgroundColor = .white
    layer.cornerRadius = 33
    layer.shadowColor = UIColor.black.cgColor
    layer.shadowOffset = CGSize(width: 0, height: 3)
    layer.shadowRadius = 20
    layer.shadowOpacity = 0.15
  }
  
}
