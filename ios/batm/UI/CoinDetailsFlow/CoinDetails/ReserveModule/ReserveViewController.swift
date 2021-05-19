import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class ReserveViewController: ModuleViewController<ReservePresenter> {
    let headerView = HeaderView()
    
    let formView = ReserveFormView()
    
    let reserveButton = MDCButton.reserve
    
    override func setupUI() {
        view.addSubviews(headerView,
                         formView,
                         reserveButton)
        
        setupDefaultKeyboardHandling()
    }
    
    override func setupLayout() {
        headerView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(25)
            $0.left.equalToSuperview().offset(15)
            $0.right.lessThanOrEqualToSuperview().offset(-15)
        }
        formView.snp.makeConstraints {
            $0.top.equalTo(headerView.snp.bottom).offset(20)
            $0.left.right.equalToSuperview().inset(15)
        }
        reserveButton.snp.makeConstraints {
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(15)
            $0.bottom.equalToSuperview().offset(-40)
        }
    }
    
    func setupUIBindings() {
        setupGesture()
        
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoadRelay.accept(()) }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.state
            .map { $0.coin?.type.code }
            .filterNil()
            .distinctUntilChanged()
            .drive(onNext: { [weak self] in
                self?.title = String(format: localize(L.Reserve.title), $0)
            })
            .disposed(by: disposeBag)
        
        presenter.state
            .map { $0.coinBalance }
            .filterNil()
            .drive(onNext: { [headerView] coinBalance in
                let amountView = CryptoFiatAmountView()
                amountView.configure(for: coinBalance)
                
                let reservedAmountView = CryptoFiatAmountView()
                reservedAmountView.configure(for: coinBalance, useReserved: true)
                
                headerView.removeAll()
                headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
                headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
                headerView.add(title: localize(L.Trades.reserved), valueView: reservedAmountView)
            })
            .disposed(by: disposeBag)
        
        let coinTypeDriver = presenter.state
            .map { $0.coin?.type }
            .filterNil()
        
        let feeDriver = presenter.state
            .map { $0.coinDetails?.txFee }
        
        Driver.combineLatest(coinTypeDriver, feeDriver)
            .drive(onNext: { [formView] in formView.configure(coinType: $0, fee: $1) })
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.currencyAmount }
            .bind(to: formView.rx.fiatAmountText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.coinAmount }
            .bind(to: formView.rx.coinAmountText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.isFieldNotEmpty }
            .bind(to: reserveButton.rx.isEnabled)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.coinAmountError }
            .bind(to: formView.rx.coinAmountErrorText)
            .disposed(by: disposeBag)
        
        reserveButton.rx.tap.asDriver()
            .drive(onNext: { [view] in view?.endEditing(true) })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let updateCoinAmountDriver = formView.rx.coinAmountText.asDriver(onErrorDriveWith: .empty())
        let maxDriver = formView.rx.maxTap
        let reserveDriver = reserveButton.rx.tap.asDriver()
        
        presenter.bind(input: ReservePresenter.Input(updateCoinAmount: updateCoinAmountDriver,
                                                     max: maxDriver,
                                                     reserve: reserveDriver))
    }
}

extension ReserveViewController: UIGestureRecognizerDelegate {
    var tapRecognizer: UITapGestureRecognizer { UITapGestureRecognizer() }
    
    func setupGesture() {
        view.addGestureRecognizer(tapRecognizer)
        
        tapRecognizer.rx.event.asDriver().map { _ in () }
            .drive(onNext: { [unowned self] in self.view.endEditing(true) })
            .disposed(by: disposeBag)
    }
}
