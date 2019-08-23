import UIKit
import RxSwift
import RxCocoa

class SeedPhraseView: UIView {
  
  let roundedView = RoundedView()
  
  let annotationLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.SeedPhrase.annotation)
    label.textColor = .slateGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    label.numberOfLines = 2
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let copyLabel: UnderlinedLabelView = {
    let view = UnderlinedLabelView()
    view.configure(for: .copy)
    return view
  }()
  
  let doneButton: MainButton = {
    let button = MainButton()
    button.configure(for: .done)
    return button
  }()
  
  var wordViews: [SeedPhraseWordView]!
  
  var horizontalStackViews: [UIStackView]!
  
  var mainStackView: UIStackView!
  
  init(flat: Bool = false) {
    super.init(frame: .null)
    
    roundedView.isHidden = flat
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(roundedView,
                annotationLabel,
                doneButton)
  }
  
  private func setupLayout() {
    roundedView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    annotationLabel.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(50)
      $0.top.greaterThanOrEqualToSuperview().offset(30)
      $0.top.equalToSuperview().offset(50).priority(.low)
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
        let view = SeedPhraseWordView(index: $0 + 1)
        view.configure(for: $1)
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
      $0.left.right.equalToSuperview().inset(10)
    }
    copyLabel.snp.makeConstraints {
      $0.top.equalTo(mainStackView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(doneButton.snp.top).offset(-30).priority(.high)
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
