import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinStakingViewController: NavigationScreenViewController<CoinStakingPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = CoinStakingHeaderView()
  
  let formView = CoinStakingFormView()
  
  let stakeButton = MDCButton.stake
  
  let unstakeButton = MDCButton.unstake
  
  let backgroundDarkView: BackgroundDarkView = {
    let view = BackgroundDarkView()
    view.alpha = 0
    return view
  }()
  
  let codeView: CodeView = {
    let view = CodeView()
    view.alpha = 0
    return view
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       stakeButton,
                                       unstakeButton)
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    [stakeButton, unstakeButton].forEach {
      $0.snp.makeConstraints {
        $0.height.equalTo(50)
        $0.top.equalTo(formView.snp.bottom).offset(5)
        $0.left.right.equalToSuperview().inset(15)
        $0.bottom.lessThanOrEqualToSuperview().offset(-30)
      }
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    codeView.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-30)
    }
  }
  
  private func showCodeView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 1
      self.codeView.alpha = 1
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [customView] in
        let title = String(format: localize(L.CoinStaking.title), $0)
        customView.setTitle(title)
      })
      .disposed(by: disposeBag)
    
    let coinBalanceDriver = presenter.state
      .map { $0.coinBalance }
      .filterNil()
    
    let stakeDetailsDriver = presenter.state
      .map { $0.stakeDetails }
      .filterNil()
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [headerView] in headerView.configure(for: $0, stakeDetails: $1) })
      .disposed(by: disposeBag)
    
    Driver.combineLatest(coinBalanceDriver, stakeDetailsDriver)
      .drive(onNext: { [formView] in formView.configure(with: $0.type.code, stakeDetails: $1) })
      .disposed(by: disposeBag)
    
    stakeDetailsDriver
      .asObservable()
      .map { $0.exist }
      .bind(to: stakeButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    stakeDetailsDriver
      .asObservable()
      .map { $0.exist && ($0.stakedDays ?? 0) >= 21 }
      .not()
      .bind(to: unstakeButton.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.code }
      .bind(to: codeView.smsCodeTextField.rx.text)
      .disposed(by: disposeBag)
    
    
    let errorMessageDriverObservable = presenter.state.asObservable()
      .map { $0.validationState }
      .mapToErrorMessage()
    let shouldShowCodePopupObservable = presenter.state.asObservable()
      .map { $0.shouldShowCodePopup }
    
    let combinedObservable = Observable.combineLatest(shouldShowCodePopupObservable,
                                                      errorMessageDriverObservable)
    
    combinedObservable
      .map { $0 ? nil : $1 }
      .subscribe(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    combinedObservable
      .map { $0 ? $1 : nil }
      .bind(to: codeView.rx.error)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.shouldShowCodePopup }
      .filter { $0 }
      .drive(onNext: { [unowned self] _ in self.showCodeView() })
      .disposed(by: disposeBag)
    
    Driver.merge(backgroundDarkView.rx.tap,
                 stakeButton.rx.tap.asDriver(),
                 unstakeButton.rx.tap.asDriver())
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver(onErrorDriveWith: .empty())
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = formView.rx.maxTap
    let stakeDriver = stakeButton.rx.tap.asDriver()
    let unstakeDriver = unstakeButton.rx.tap.asDriver()
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinStakingPresenter.Input(back: backDriver,
                                                     updateCoinAmount: updateCoinAmountDriver,
                                                     updateCode: updateCodeDriver,
                                                     cancel: cancelDriver,
                                                     max: maxDriver,
                                                     stake: stakeDriver,
                                                     unstake: unstakeDriver,
                                                     sendCode: sendCodeDriver))
  }
}
