
import Foundation
import PhoneNumberKit

enum TransferReceiverAction {
    case setupContacts([CGroup])
    case updatePhone(String)
    case selectedContact(BContact)
    case reset
}

struct TransferReceiverState {
    
    var contacts: [CGroup]?
    var phone: String?
    var selectedContact: BContact?
    var phoneNumberError: String?
    
    var phoneE164: String {
        guard let phoneNumber = try? PhoneNumberKit.default.parse(phone ?? "") else { return "" }
        
        return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
    }
    
    var isAllFieldsNotEmpty: Bool {
        return phone?.count ?? 0 > 0
    }
    
    var filteredContacts: [CGroup]? {
        guard let enteredPhone = self.phone, let initContacts = contacts else { return contacts}
        let filteredContacts = initContacts.map{ $0.copy() as! CGroup }
        filteredContacts.forEach {$0.filterContacts(phone: enteredPhone) }
        var groups = filteredContacts.filter{ $0.filteredContacts?.count ?? 0 > 0 }
        
        var emptyGroups = [CGroup]()
        for group in groups {
            if group.contacts.isEmpty {
                emptyGroups.append(group)
            }
        }
        
        let isGroupEmpty = groups.count == emptyGroups.count
        
        groups = isGroupEmpty ? [] : groups
        return groups
    }
}

final class TransferSelectReceiverStore: ViewStore<TransferReceiverAction, TransferReceiverState> {
    override var initialState: TransferReceiverState {
        return TransferReceiverState()
    }
    
    override func reduce(state: TransferReceiverState, action: TransferReceiverAction) -> TransferReceiverState {
        var state = state
        switch action {
        case let .setupContacts(contacts):
            state.contacts = contacts
        case let .updatePhone(phone):
            state.phone = PartialFormatter.default.formatPartial(phone)
            state.phoneNumberError = nil
        case let .selectedContact(contact):
            state.selectedContact = contact
            state.phoneNumberError = nil
        case .reset:
            state.selectedContact = nil
            state.phone = nil
        }
        return state
    }
    
}
