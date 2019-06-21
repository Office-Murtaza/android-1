import UIKit
import RxSwift
import RxCocoa

class SeedPhraseView: RoundedView {
  
  let annotationLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.SeedPhrase.annotation)
    label.textColor = .slateGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    label.numberOfLines = 0
    return label
  }()
  
  let copyLabel = CopyLabelView()
  
  let doneButton: MainButton = {
    let button = MainButton()
    button.configure(for: .done)
    return button
  }()
  
  var wordViews: [SeedPhraseWordView]!
  
  var horizontalStackViews: [UIStackView]!
  
  var mainStackView: UIStackView!
  
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
                doneButton)
  }
  
  private func setupLayout() {
    annotationLabel.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(50)
    }
    doneButton.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(45)
      $0.bottom.equalToSuperview().offset(-30)
    }
  }
  
  func configure(for words: [String]) {
    initializeMainStackView(words: words)
    setupMainStackViewUI()
    setupMainStackViewLayout()
  }
  
  private func initializeMainStackView(words: [String]) {
    wordViews = {
      return words.enumerated().map {
        let view = SeedPhraseWordView()
        view.configure(for: $1, by: $0 + 1)
        return view
      }
    }()
    
    horizontalStackViews = {
      return wordViews.chunked(into: 3).map {
        let stackView = UIStackView()
        stackView.spacing = 8
        stackView.addArrangedSubviews($0)
        return stackView
      }
    }()
    
    mainStackView = {
      let stackView = UIStackView()
      stackView.axis = .vertical
      stackView.spacing = 8
      stackView.alignment = .center
      stackView.addArrangedSubviews(horizontalStackViews)
      return stackView
    }()
  }
  
  private func setupMainStackViewUI() {
    addSubviews(mainStackView,
                copyLabel)
  }
  
  private func setupMainStackViewLayout() {
    mainStackView.snp.makeConstraints {
      $0.top.equalTo(annotationLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(30)
    }
    copyLabel.snp.makeConstraints {
      $0.top.equalTo(mainStackView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
    }
  }
  
}

extension Reactive where Base == SeedPhraseView {
  var copyTap: Driver<Void> {
    return base.copyLabel.rx.tap
  }
  var doneTap: Driver<Void> {
    return base.doneButton.rx.tap.asDriver()
  }
}
