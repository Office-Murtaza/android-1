import UIKit
import RxSwift
import RxCocoa

class RecoverSeedPhraseView: RoundedView {
  
  let errorView: ErrorView = {
    let view = ErrorView()
    view.isHidden = true
    return view
  }()
  
  let annotationLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.RecoverSeedPhrase.annotation)
    label.textColor = .slateGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    label.numberOfLines = 2
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let pasteLabel: UnderlinedLabelView = {
    let view = UnderlinedLabelView()
    view.configure(for: .paste)
    return view
  }()
  
  let doneButton: MainButton = {
    let button = MainButton()
    button.configure(for: .done)
    return button
  }()
  
  let wordViews: [SeedPhraseWordView] = {
    return Array(1...12).map { return SeedPhraseWordView(index: $0, isEditable: true) }
  }()
  
  lazy var horizontalStackViews: [UIStackView]! = {
    return wordViews.chunked(into: 3).map {
      let stackView = UIStackView()
      stackView.spacing = 8
      stackView.addArrangedSubviews($0)
      return stackView
    }
  }()
  
  lazy var mainStackView: UIStackView! = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 8
    stackView.alignment = .center
    stackView.addArrangedSubviews(horizontalStackViews)
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
    
    addSubviews(annotationLabel,
                mainStackView,
                pasteLabel,
                doneButton,
                errorView)
  }
  
  private func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    annotationLabel.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(50)
      $0.top.greaterThanOrEqualToSuperview().offset(30)
      $0.top.equalToSuperview().offset(50).priority(.low)
    }
    mainStackView.snp.makeConstraints {
      $0.top.equalTo(annotationLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(10)
    }
    pasteLabel.snp.makeConstraints {
      $0.top.equalTo(mainStackView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(doneButton.snp.top).offset(-30).priority(.high)
    }
    doneButton.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(45)
      $0.bottom.equalToSuperview().offset(-30)
    }
  }
  
  func configure(for words: [String]) {
    for (index, word) in words.enumerated() {
      wordViews[index].configure(for: word)
    }
  }
}

extension Reactive where Base == RecoverSeedPhraseView {
  var pasteTap: Driver<Void> {
    return base.pasteLabel.rx.tap
  }
  var doneTap: Driver<[String]> {
    return base.doneButton.rx.tap
      .asDriver()
      .map { [base] in
        base.wordViews
          .map { $0.textField.text }
          .map { $0?.nilIfEmpty() }
          .compactMap { $0 }
      }
  }
  var error: Binder<String?> {
    return Binder(base) { target, value in
      target.errorView.isHidden = value == nil
      target.errorView.configure(for: value)
    }
  }
}
