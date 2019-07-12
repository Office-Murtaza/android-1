import UIKit
import RxSwift
import RxCocoa

class PinCodeView: UIView {
  
  static let numberOfDots = 6
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 8
    return stackView
  }()
    
  let dots: [UIView] = {
    return Array(0..<numberOfDots).map { _ in
      let view = PinCodeDotView()
      view.backgroundColor = .cadetBlue
      return view
    }
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
    
    addSubview(stackView)
    stackView.addArrangedSubviews(dots)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == PinCodeView {
  var currentCount: Binder<Int> {
    return Binder(base) { target, count in
      let maxCount = min(max(0, count), PinCodeView.numberOfDots)
      target.dots.forEach { $0.backgroundColor = .cadetBlue }
      (0..<maxCount).forEach { target.dots[$0].backgroundColor = .lightGold }
    }
  }
}
