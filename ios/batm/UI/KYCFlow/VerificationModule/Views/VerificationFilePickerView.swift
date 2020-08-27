import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class VerificationFilePickerView: UIView, HasDisposeBag {
  
  struct Metrics {
    static let height: CGFloat = 190
  }
  
  let container: UIView = {
    let view = UIView()
    view.backgroundColor = .duckEggBlue
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 4
    return view
  }()
  
  let selectedImageView = UIImageView(image: nil)
  
  let removeButton = MDCButton.close
  let selectButton = MDCButton.plus
  
  let errorLabel: UILabel = {
    let label = UILabel()
    label.textColor = .tomato
    label.textAlignment = .center
    label.font = .systemFont(ofSize: 16)
    label.numberOfLines = 0
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
                errorLabel)
    container.addSubviews(removeButton,
                          selectButton)
  }
  
  private func setupLayout() {    
    [removeButton, selectButton].forEach {
      $0.snp.makeConstraints {
        $0.size.equalTo(35)
        $0.bottom.right.equalToSuperview().inset(10)
      }
    }
    
    errorLabel.snp.makeConstraints {
      $0.top.equalTo(container.snp.bottom).offset(15)
      $0.left.right.bottom.equalToSuperview()
    }
    
    removeImage()
  }
  
  func showImage(_ image: UIImage) {
    selectedImageView.image = image
    
    if selectedImageView.superview == nil {
      container.insertSubview(selectedImageView, at: 0)
    }
    
    selectedImageView.snp.remakeConstraints {
      $0.edges.equalToSuperview()
      $0.keepRatio(for: selectedImageView)
    }
    
    container.snp.remakeConstraints {
      $0.top.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview()
      $0.right.lessThanOrEqualToSuperview()
      $0.height.equalTo(Metrics.height)
    }
    
    removeButton.isHidden = false
    selectButton.isHidden = true
  }
  
  func removeImage() {
    selectedImageView.image = nil
    selectedImageView.removeFromSuperview()
    
    container.snp.remakeConstraints {
      $0.top.centerX.equalToSuperview()
      $0.size.equalTo(Metrics.height)
    }
    
    removeButton.isHidden = true
    selectButton.isHidden = false
  }
}

extension Reactive where Base == VerificationFilePickerView {
  var select: Driver<Void> {
    return base.selectButton.rx.tap.asDriver()
  }
  var remove: Driver<Void> {
    return base.removeButton.rx.tap.asDriver()
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
  var imageErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.errorLabel.text = value
    }
  }
}
