import UIKit
import RxSwift
import RxCocoa
import SnapKit

class SeedPhraseWordView: UIView, UITextFieldDelegate, HasDisposeBag {
  
  let index: Int
  
  let container = UIView()
  
  let dummyButton = DummyButton()
  
  lazy var indexLabel: UILabel = {
    let label = UILabel()
    label.text = "\(index)"
    label.textColor = .pinkishGreyTwo
    label.font = .poppinsMedium11
    return label
  }()
  
  lazy var textField: TextFieldWithoutPaddings = {
    let textField = TextFieldWithoutPaddings()
    textField.font = .poppinsMedium11
    textField.textColor = .slateGrey
    textField.delegate = self
    return textField
  }()
  
  override var intrinsicContentSize: CGSize {
    return CGSize(width: 60, height: 28)
  }
  
  init(index: Int, isEditable: Bool = false) {
    self.index = index
    
    super.init(frame: .null)
    
    self.dummyButton.isUserInteractionEnabled = isEditable
    self.textField.isUserInteractionEnabled = isEditable
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    layer.cornerRadius = 3
    layer.borderColor = UIColor.whiteFive.cgColor
    layer.borderWidth = 1
    
    addSubviews(container,
                dummyButton)
    container.addSubviews(indexLabel,
                          textField)
  }
  
  private func setupLayout() {
    setContentHuggingPriority(.init(1), for: .vertical)
    setContentHuggingPriority(.init(1), for: .horizontal)
    container.snp.makeConstraints {
      $0.center.equalToSuperview()
      $0.left.top.greaterThanOrEqualToSuperview().offset(8).priority(.required)
      $0.right.bottom.lessThanOrEqualToSuperview().offset(-8).priority(.required)
    }
    dummyButton.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    indexLabel.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func showTextField() {
    container.addSubview(textField)
    
    indexLabel.snp.remakeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    textField.snp.remakeConstraints {
      $0.left.equalTo(indexLabel.snp.right).offset(4)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  private func hideTextField() {
    textField.removeFromSuperview()
    
    indexLabel.snp.remakeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func updateTextFieldVisibility() {
    let shouldBeHidden = (textField.text ?? "").isEmpty
    
    if shouldBeHidden {
      hideTextField()
    } else {
      showTextField()
    }
  }
  
  func configure(for word: String) {
    textField.text = word
    updateTextFieldVisibility()
  }
  
  func textFieldDidEndEditing(_ textField: UITextField) {
    updateTextFieldVisibility()
    dummyButton.isHidden = false
  }
  
  func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    textField.resignFirstResponder()
    return true
  }
  
  private func setupBindings() {
    dummyButton.rx.tap.asDriver()
      .drive(onNext: { [unowned self] in
        self.dummyButton.isHidden = true
        self.showTextField()
        self.textField.becomeFirstResponder()
      })
      .disposed(by: disposeBag)
  }
}
