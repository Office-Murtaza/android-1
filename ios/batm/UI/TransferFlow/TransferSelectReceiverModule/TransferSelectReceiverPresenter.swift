import Foundation
import RxSwift
import RxCocoa


class TransferSelectReceiverPresenter: ModulePresenter, TransferModule {
    
    typealias Store = ViewStore<TransferReceiverAction, TransferReceiverState>
    
    weak var delegate: TransferModuleDelegate?
    private let store: Store
    private let fetchDataRelay = PublishRelay<Void>()
    private let contactManager = ContactsManager()
    
    struct Input {
       var phoneText: Driver<String?>
       var next: Driver<Void>
       var selectedContact: Driver<BContact?>
    }
    
    var state: Driver<TransferReceiverState> {
        return store.state
    }
    
    init(store: Store = TransferSelectReceiverStore()) {
        self.store = store
    }
    
    func resetState() {
        store.action.accept(.reset)
    }
    
    func bind(input: Input) {
        fetchDataRelay.asObservable().subscribe { [weak self] (_) in
            guard let contacts = self?.contactManager.getContacts() else { return }
            self?.store.action.accept(.setupContacts(contacts))
        }.disposed(by: disposeBag)
        
        input.phoneText.asObservable()
            .filterNil()
            .map{ TransferReceiverAction.updatePhone($0)}
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.selectedContact.asObservable()
            .filterNil()
            .map{ TransferReceiverAction.selectedContact($0)}
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.next
            .asObservable()
            .subscribe(onNext: { [weak self, delegate] in
                if let selectedContact = self?.store.currentState.selectedContact,
                   selectedContact.phones.first == self?.store.currentState.phone {
                    delegate?.showSendGift(contact: selectedContact)
                } else if let phone = self?.store.currentState.phone {
                    let contact = BContact(phones: [phone])
                    delegate?.showSendGift(contact: contact)
                }
            })
            .disposed(by: disposeBag)
        
        fetchDataRelay.accept(())
    }
    
}
