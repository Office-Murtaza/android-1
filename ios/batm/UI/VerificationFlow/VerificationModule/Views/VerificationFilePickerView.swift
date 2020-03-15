import UIKit
import RxSwift
import RxCocoa

final class VerificationFilePickerView: UIView, HasDisposeBag {
  
  struct Metrics {
    static let height: CGFloat = 160
  }
  
  let container: UIView = {
    let view = UIView()
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 16
    return view
  }()
  
  let selectedImageView = UIImageView(image: nil)
  
  let scanPlaceholderView: UIView = {
    let view = UIView()
    view.backgroundColor = .whiteTwo
    return view
  }()
  
  let scanPlaceholderImageView = UIImageView(image: UIImage(named: "grey_plus"))
  
  let selectStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.alignment = .fill
    stackView.spacing = 50
    return stackView
  }()
  
  let selectLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .select)
    return label
  }()
  
  let removeLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .remove)
    label.isHidden = true
    return label
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
    
    addSubviews(container,
                selectStackView)
    scanPlaceholderView.addSubview(scanPlaceholderImageView)
    selectStackView.addArrangedSubviews(selectLabel,
                                        removeLabel)
  }
  
  private func setupLayout() {
    container.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(160)
    }
    scanPlaceholderImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    selectStackView.snp.makeConstraints {
      $0.top.equalTo(container.snp.bottom).offset(15)
      $0.bottom.centerX.equalToSuperview()
    }
    
    removeImage()
  }
  
  func showImage(_ image: UIImage) {
    selectedImageView.image = image
    removeLabel.isHidden = false
    
    if selectedImageView.superview == nil {
      container.subviews.forEach { $0.removeFromSuperview() }
      container.addSubview(selectedImageView)
    }
    
    selectedImageView.snp.remakeConstraints {
      $0.edges.equalToSuperview()
      $0.keepRatio(for: selectedImageView)
    }
  }
  
  func removeImage() {
    selectedImageView.image = nil
    removeLabel.isHidden = true
    
    if scanPlaceholderView.superview == nil {
      container.subviews.forEach { $0.removeFromSuperview() }
      container.addSubview(scanPlaceholderView)
    }
    
    scanPlaceholderView.snp.remakeConstraints {
      $0.edges.equalToSuperview()
      $0.width.equalTo(scanPlaceholderView.snp.height)
    }
  }
}

extension Reactive where Base == VerificationFilePickerView {
  var select: Driver<Void> {
    return base.selectLabel.rx.tap
  }
  var remove: Driver<Void> {
    return base.removeLabel.rx.tap
  }
  var image: Binder<UIImage?> {
    return Binder(base) { target, value in
      if let image = value {
        target.showImage(image)
      } else {
        target.removeImage()
      }
    }
  }
}
