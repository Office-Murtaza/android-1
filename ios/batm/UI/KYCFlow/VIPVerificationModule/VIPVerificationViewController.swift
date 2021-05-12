import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class VIPVerificationViewController: ModuleViewController<VIPVerificationPresenter> {
    private lazy var rootScrollView = RootScrollView()
    private lazy var filePickerView = VerificationFilePickerView()
    private lazy var formView = VIPVerificationFormView()
    private lazy var sendButton = MDCButton.send
    
    override func setupUI() {
        title = localize(L.VIPVerification.title)
        
        view.addSubviews(rootScrollView)
        
        rootScrollView.contentInsetAdjustmentBehavior = .never
        rootScrollView.contentView.addSubviews(filePickerView,
                                               formView,
                                               sendButton)
        filePickerView.setImageContainer(with: localize(L.KYC.Button.addIDSelfie))
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
        filePickerView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(16)
            $0.left.right.equalToSuperview().inset(16)
        }
        formView.snp.makeConstraints {
            $0.top.equalTo(filePickerView.snp.bottom).offset(16)
            $0.left.right.equalToSuperview().inset(16)
            $0.bottom.lessThanOrEqualTo(sendButton.snp.top).offset(-5)
        }
        sendButton.snp.makeConstraints {
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(16)
            $0.bottom.equalToSuperview().offset(-40)
        }
    }
    
    func setupUIBindings() {
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoad.accept(())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.selectedImage }
            .bind(to: filePickerView.rx.image)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.imageError }
            .bind(to: filePickerView.rx.imageErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.ssn }
            .bind(to: formView.rx.ssnText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.ssnError }
            .bind(to: formView.rx.ssnErrorText)
            .disposed(by: disposeBag)
        
        Driver.merge(filePickerView.rx.select,
                     filePickerView.rx.remove,
                     sendButton.rx.tap.asDriver())
            .drive(onNext: { [unowned self] in self.view.endEditing(true) })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let selectDriver = filePickerView.rx.select
        let removeDriver = filePickerView.rx.remove
        let updateSSNDriver = formView.ssnTextField.rx.text.asDriver()
        let sendDriver = sendButton.rx.tap.asDriver()
        
        presenter.bind(input: VIPVerificationPresenter.Input(select: selectDriver,
                                                             remove: removeDriver,
                                                             updateSSN: updateSSNDriver,
                                                             send: sendDriver))
    }
}
