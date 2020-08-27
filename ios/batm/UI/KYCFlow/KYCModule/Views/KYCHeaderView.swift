import UIKit
import RxSwift
import RxCocoa

class HeaderView: UIView {
  
  let mainStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 20
    return stackView
  }()
  
  let titleStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.alignment = .leading
    stackView.spacing = 15
    return stackView
  }()
  
  let valueStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.alignment = .leading
    stackView.spacing = 15
    return stackView
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
    
    addSubview(mainStackView)
    
    mainStackView.addArrangedSubviews(titleStackView,
                                      valueStackView)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func removeAll() {
    titleStackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
    valueStackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
  }
  
  func add(title: String, valueView: UIView) {
    let titleView = UIView()
    
    let titleLabel = UILabel()
    titleLabel.text = title
    titleLabel.textColor = .warmGrey
    titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    
    titleView.addSubview(titleLabel)
    
    titleLabel.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    
    titleStackView.addArrangedSubview(titleView)
    valueStackView.addArrangedSubview(valueView)
    
    titleView.snp.makeConstraints {
      $0.height.equalTo(valueView)
    }
  }
  
  func add(title: String, value: String, applyStyle: ((UILabel) -> Void)? = nil) {
    let valueLabel = UILabel()
    valueLabel.text = value
    valueLabel.textColor = .slateGrey
    valueLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    applyStyle?(valueLabel)
    
    add(title: title, valueView: valueLabel)
  }
}
