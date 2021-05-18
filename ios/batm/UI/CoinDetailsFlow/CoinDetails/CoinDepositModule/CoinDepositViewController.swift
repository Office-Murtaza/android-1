import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinDepositViewController: ModuleViewController<CoinDepositPresenter> {
    let qrCodeCardView = QRCodeCardView()
    
    override func setupUI() {
        view.addSubview(qrCodeCardView)
    }
    
    override func setupLayout() {
        qrCodeCardView.snp.makeConstraints {
            $0.top.left.right.equalToSuperview().inset(30)
        }
    }
    
    func setupUIBindings() {
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoadRelay.accept(()) }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.didCoinLoadRelay
            .asDriver(onErrorDriveWith: .empty())
            .drive(onNext: { [weak self] in
                self?.title = String(format: localize(L.CoinDeposit.title), $0?.type.code ?? "")
                self?.qrCodeCardView.configure(for: $0?.address ?? "")
            })
            .disposed(by: disposeBag)
        
        qrCodeCardView.rx.copy
            .drive(onNext: { [unowned self] in self.view.makeToast(localize(L.Shared.copied)) })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        let copyDriver = qrCodeCardView.rx.copy
        presenter.bind(input: CoinDepositPresenter.Input(copy: copyDriver))
    }
}
