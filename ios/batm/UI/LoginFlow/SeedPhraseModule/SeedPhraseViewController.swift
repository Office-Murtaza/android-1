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

    let pasteButton = MDCButton.secondaryPaste
    
    lazy var generateButton: MDCButton = {
        let button = MDCButton.secondaryText
        button.setTitle(localize(L.SeedPhrase.generate), for: .normal)
        button.setImage(UIImage(named: "key"), for: .normal)
        button.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 10)
        return button 
    }();

    lazy var buttonStack: UIStackView = {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.distribution = .fillProportionally
        stack.spacing = 5
        
        return stack
    }()
    
    
  let nextButton = MDCButton.next
    
  override func setupUI() {
    title = localize(L.SeedPhrase.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           infoView,
                                           formView,
                                           copyButton,
                                           nextButton,
                                           buttonStack)
    
    buttonStack.addArrangedSubviews([
        generateButton,
        pasteButton,
        copyButton
    ])
    
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
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    infoView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(infoView.snp.bottom).offset(10)
      $0.left.right.equalToSuperview().inset(15)
    }

    buttonStack.snp.makeConstraints{
        $0.left.equalTo(formView.snp.left)
        $0.right.equalTo(formView.snp.right)
        $0.top.equalTo(formView.snp.bottom).offset(10)
        $0.height.equalTo(35)
    }
    
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    
    pasteButton.rx.tap.asDriver()
      .map { UIPasteboard.general.string ?? "" }
      .map { $0.separatedWords }
      .drive(onNext: { [formView] in
              formView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    
    presenter.state
        .map{ $0.generatedPhrase }
        .distinctUntilChanged()
        .drive(onNext: { [formView] in
                formView.configure(for: $0)
            
        })
        .disposed(by: disposeBag)
    
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.mode }
      .drive(onNext: { [nextButton] mode in
        switch mode {
        case .creation:
          nextButton.setTitle(localize(L.Shared.Button.next), for: .normal)
        case .showing:
          nextButton.setTitle(localize(L.Shared.Button.done), for: .normal)
        }
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
    let generateDriver = generateButton.rx.tap.asDriver()
    let updateSeedPhraseDriver = formView.rx.seedPhrase
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: SeedPhrasePresenter.Input(copy: copyDriver,
                                                    next: nextDriver,
                                                    generate: generateDriver,
                                                    updateSeedPhrase: updateSeedPhraseDriver))
  }
}
