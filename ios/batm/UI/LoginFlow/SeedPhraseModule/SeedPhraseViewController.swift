import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class SeedPhraseViewController: ModuleViewController<SeedPhrasePresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let infoView: InfoView = {
    let view = InfoView()
    view.setup(with: localize(L.SeedPhrase.annotation))
    return view
  }()
  
  let formView = SeedPhraseFormView()
  
  let copyButton = MDCButton.secondaryCopy
  
  let nextButton = MDCButton.next
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    title = localize(L.SeedPhrase.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           infoView,
                                           formView,
                                           copyButton,
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
    infoView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(infoView.snp.bottom).offset(5)
      $0.left.right.equalToSuperview().inset(15)
    }
    copyButton.snp.makeConstraints {
      $0.top.equalTo(formView.snp.bottom).offset(15)
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
      .map { $0.seedPhrase.separatedWords }
      .drive(onNext: { [formView] in formView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    copyButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let copyDriver = copyButton.rx.tap.asDriver()
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: SeedPhrasePresenter.Input(copy: copyDriver,
                                                    next: nextDriver))
  }
}
