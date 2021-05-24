import UIKit
import Contacts

struct BContact: Equatable {
    let name: String?
    let phones: [String]
    let image: UIImage?
    
    init(name: String? = nil, phones: [String], image: UIImage? = nil) {
        self.name = name
        self.phones = phones
        self.image = image
    }
}

typealias ContactGroup = (key: String, value: [BContact])

class CGroup: NSObject, NSCopying {
    var key: String
    var contacts: [BContact]
    var filteredContacts: [BContact]?
    
    init(key: String, contacts: [BContact]) {
        self.key = key
        self.contacts = contacts
        self.filteredContacts = contacts
    }
    
    func filterContacts(phone: String) {
        if phone.isEmpty {
            filteredContacts = contacts
            return
        }
        filteredContacts = contacts.filter({ (contact) -> Bool in
            let result = contact.phones.filter { $0.contains(phone) }
            return result.isNotEmpty
        })
        
        if (filteredContacts?.isEmpty).value {
            filteredContacts = contacts.filter { ($0.name?.lowercased().contains(phone.lowercased())).value }
        }
    }
    
    func copy(with zone: NSZone? = nil) -> Any {
           let copy = CGroup(key: key, contacts: contacts)
           return copy
    }
}

class ContactsManager {
    
    func getContacts() -> [CGroup]? {
        
        let store = CNContactStore()

        var bContats: [BContact] = []
        let fetchRequest = CNContactFetchRequest(keysToFetch: fetchKeys)
        fetchRequest.sortOrder = CNContactSortOrder.givenName
        do {
            try store.enumerateContacts(with: fetchRequest, usingBlock: { ( contact, error) -> Void in
                bContats.append(BContact(name: contact.contactFullName() ?? "",
                                         phones: contact.getPhones(),
                                         image: contact.getContactImage()))
            })
            
            var contactMap = [String: [BContact]]()
            
            bContats.forEach { (contact) in
                let name = contact.name ?? "undefined"
                let key = String(name[name.startIndex])
                if var dataArray = contactMap[key] {
                    dataArray.append(contact)
                    contactMap[key] = dataArray
                } else {
                    contactMap[key] = [contact]
                }
            }
            
            let sortedMap = contactMap.sorted(by: {$0.0 < $1.0}).map { (key: String, value: [BContact]) -> CGroup in
                return CGroup(key: key, contacts: value)
            }
            
            return sortedMap
        }
        catch let error as NSError {
            print(error.localizedDescription)
        }
       
        return nil
    }

    lazy var fetchKeys: [CNKeyDescriptor] = {
        return [
            CNContactNamePrefixKey,
            CNContactGivenNameKey,
            CNContactMiddleNameKey,
            CNContactFamilyNameKey,
            CNContactImageDataKey,
            CNContactThumbnailImageDataKey,
            CNContactPhoneNumbersKey,
        ] as [CNKeyDescriptor]
    }()
}
