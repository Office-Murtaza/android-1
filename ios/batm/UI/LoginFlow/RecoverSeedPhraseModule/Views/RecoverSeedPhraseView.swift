import UIKit
import RxSwift
import RxCocoa

class RecoverSeedPhraseView: RoundedView {
  
  let annotationLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.RecoverSeedPhrase.annotation)
    label.textColor = .slateGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    label.numberOfLines = 0
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
                doneButton)
  }
  
  private func setupLayout() {
    annotationLabel.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(50)
    }
    mainStackView.snp.makeConstraints {
      $0.top.equalTo(annotationLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(30)
    }
    pasteLabel.snp.makeConstraints {
      $0.top.equalTo(mainStackView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
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
}
