import UIKit
import Contacts
import ContactsUI
import MaterialComponents

final class TransferSelectReceiverViewController:ModuleViewController<TransferSelectReceiverPresenter> {
    
    let contactManager = ContactsManager()
    let dataSource = TransferTableDataSource()
    let nextButton = MDCButton.next
    
    let dismissKeyboardView = TransferHideKeyboardView()
    
    private lazy var header: TransferSelectReceiverHeader = {
        let headerFrame = CGRect(x: 0, y: 0, width: 0, height: 125)
        let header = TransferSelectReceiverHeader(frame: headerFrame)
        return header
    }()

    private lazy var tableView: UITableView = {
       let tableView = UITableView()
        dataSource.configure(tableView: tableView)
        return tableView
    }()

    override func setupUI() {
        title = localize(L.Transfer.Receiver.title)
        view.addSubviews([dismissKeyboardView, tableView, header, nextButton])
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        presenter.resetState()
        header.resetPhone()
    }
    
    func bind() {
        presenter.state
            .asObservable()
            .map{ $0.filteredContacts }
            .filterNil().subscribe { [weak self] result in
                guard let group =  result.element else { return }
                self?.tableView.isHidden = group.count == 0
                self?.dataSource.reloadContacts(group)
            }.disposed(by: disposeBag)

        dataSource.selectedContact.asObservable().filterNil().subscribe { [weak self] result in
            let phone = result.element?.phones.first
            self?.header.phoneNumberTextField.becomeFirstResponder()
            self?.header.phoneNumberTextField.text = phone
            self?.view.endEditing(true)
        }.disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.phone }
            .bind(to: header.phoneNumberTextField.rx.text)
            .disposed(by: disposeBag)
        
        presenter.state
          .asObservable()
            .map { $0.phoneNumberError }
            .bind(to: header.rx.phoneErrorText)
            .disposed(by: disposeBag)
        
        dismissKeyboardView.callback = { [weak self] in
            self?.view.endEditing(true)
        }
        
        presenter.state
            .asObservable()
            .map { $0.phoneE164.count > 0 && $0.isAllFieldsNotEmpty }
            .bind(to: nextButton.rx.isEnabled)
            .disposed(by: disposeBag)
    }

    override func setupLayout() {
        
        dismissKeyboardView.snp.makeConstraints {
            $0.top.equalTo(header.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.bottom.equalTo(nextButton.snp.top)
        }
        
        header.snp.makeConstraints{
            $0.top.equalToSuperview().offset(15)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(75)
        }
        
        tableView.snp.makeConstraints {
            $0.top.equalTo(header.snp.bottom).offset(25)
            $0.left.right.equalToSuperview()
            $0.bottom.equalTo(nextButton.snp.top).offset(-10)
        }
        
        nextButton.snp.makeConstraints {
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(15)
            $0.bottom.equalToSuperview().offset(-40)
          }
    }
  
    override func setupBindings() {
        bind()
        let phoneDriver = header.rx.text.asDriver()
        let nextDriver = nextButton.rx.tap.asDriver()
        let selectedContact = dataSource.selectedContact.asDriver()
        presenter.bind(input: TransferSelectReceiverPresenter.Input(phoneText: phoneDriver,
                                                                    next: nextDriver,
                                                                    selectedContact: selectedContact))
        
    }
}
