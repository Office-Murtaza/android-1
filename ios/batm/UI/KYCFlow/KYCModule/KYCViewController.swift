import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class KYCViewController: ModuleViewController<KYCPresenter> {
    var dataSource: KYCDataSource?
    
    private lazy var verifyButton = MDCButton.verify
    private lazy var tableView = KYCTableView()
    
    override func viewDidAppear(_ animated: Bool) {
        presenter.didViewLoad.accept(())
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        guard let header = tableView.tableHeaderView else { return }
        header.frame.size.height = header.systemLayoutSizeFitting(CGSize(width: view.bounds.width - 32.0, height: 0)).height
    }
    
    override func setupUI() {
        title = localize(L.KYC.title)
        view.addSubviews([tableView, verifyButton])
        verifyButton.isHidden = true
    }
    
    override func setupLayout() {
        tableView.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.left.equalToSuperview().offset(16)
            $0.right.equalToSuperview().offset(-16)
            $0.bottom.equalTo(verifyButton.snp.top).offset(-32)
        }
        verifyButton.snp.makeConstraints {
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(16)
            $0.top.equalTo(tableView.snp.bottom).offset(32)
            $0.bottom.greaterThanOrEqualToSuperview().offset(-32)
        }
    }
    
    func setupUIBindings() {
        tableView.dataSource = dataSource
        dataSource?.tableView = tableView
        
        presenter.kycRelay
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] in
                self?.setup(kyc: $0)
            })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        let verifyDriver = verifyButton.rx.tap.asDriver()
        presenter.bind(input: KYCPresenter.Input(verify: verifyDriver))
    }
    
    private func setup(kyc: KYC) {
        dataSource?.kycRelay.accept(kyc)
        verifyButton.isHidden = !kyc.status.needAnyVerification
        
        if kyc.status.needVerification {
            verifyButton.setTitle(localize(L.KYC.Button.verify), for: .normal)
        } else if kyc.status.needVIPVerification {
            verifyButton.setTitle(localize(L.KYC.Button.vipVerify), for: .normal)
        }
    }
}
