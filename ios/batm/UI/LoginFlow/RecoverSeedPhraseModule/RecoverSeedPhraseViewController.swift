import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class RecoverSeedPhraseViewController: ModuleViewController<RecoverSeedPhrasePresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let annotationLabel: UILabel = {
    let paragraphStyle = NSMutableParagraphStyle()
    paragraphStyle.lineSpacing = 10
    
    let text = localize(L.RecoverSeedPhrase.annotation)
    let attributedString = NSAttributedString(string: text, attributes: [.foregroundColor: UIColor.warmGrey,
                                                                         .font: UIFont.systemFont(ofSize: 16),
                                                                         .paragraphStyle: paragraphStyle])
  
    let label = UILabel()
    label.attributedText = attributedString
    label.textAlignment = .center
    label.numberOfLines = 0
    return label
  }()
  
  let formView = RecoverSeedPhraseFormView()
  
  let pasteButton = MDCButton.secondaryPaste
  
  let nextButton = MDCButton.next
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    title = localize(L.RecoverSeedPhrase.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           annotationLabel,
                                           formView,
                                           pasteButton,
                                           nextButton)
    
    setupDefaultKeyboardHandling()
  }
  
  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    annotationLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(50)
      $0.right.lessThanOrEqualToSuperview().offset(-50)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(annotationLabel.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
    }
    pasteButton.snp.makeConstraints {
      $0.top.equalTo(formView.snp.bottom).offset(20)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(nextButton.snp.top).offset(-20)
      $0.width.equalTo(75)
      $0.height.equalTo(36)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    pasteButton.rx.tap.asDriver()
      .map { UIPasteboard.general.string ?? "" }
      .map { $0.separatedWords }
      .drive(onNext: { [formView] in formView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updateSeedPhraseDriver = formView.rx.seedPhrase
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: RecoverSeedPhrasePresenter.Input(updateSeedPhrase: updateSeedPhraseDriver,
                                                           next: nextDriver))
  }
}
