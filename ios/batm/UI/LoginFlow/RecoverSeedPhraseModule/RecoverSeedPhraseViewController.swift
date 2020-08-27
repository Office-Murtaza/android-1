import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class RecoverSeedPhraseViewController: ModuleViewController<RecoverSeedPhrasePresenter> {
  
  let rootScrollView = RootScrollView()
  
  let infoView: InfoView = {
    let view = InfoView()
    view.setup(with: localize(L.RecoverSeedPhrase.annotation))
    return view
  }()
  
  let formView = RecoverSeedPhraseFormView()
  
  let pasteButton = MDCButton.secondaryPaste
  
  let errorLabel: UILabel = {
    let label = UILabel()
    label.textColor = .tomato
    label.textAlignment = .center
    label.font = .systemFont(ofSize: 16)
    label.numberOfLines = 0
    return label
  }()
  
  let nextButton = MDCButton.next
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    title = localize(L.RecoverSeedPhrase.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(infoView,
                                           formView,
                                           pasteButton,
                                           errorLabel,
                                           nextButton)
    
    setupDefaultKeyboardHandling()
  }
  
  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.greaterThanOrEqualToSuperview()
    }
    infoView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(infoView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(15)
    }
    pasteButton.snp.makeConstraints {
      $0.top.equalTo(formView.snp.bottom).offset(20)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(errorLabel.snp.top).offset(-20)
      $0.width.equalTo(75)
      $0.height.equalTo(36)
    }
    errorLabel.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalTo(nextButton.snp.top).offset(-25)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.seedPhraseError }
      .bind(to: errorLabel.rx.text)
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
