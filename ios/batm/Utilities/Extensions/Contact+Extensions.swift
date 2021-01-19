import UIKit
import Contacts

extension CNContact{
    func getPhones() -> [String] {
       return phoneNumbers.compactMap{ (phoneNumber) -> String? in
        guard let number = phoneNumber.value as? CNPhoneNumber else { return nil }
        return number.stringValue
        }
    }
    
    func getContactImage() -> UIImage? {
        guard let data = imageData else { return nil }
        return UIImage(data: data)
    }
    
   func contactFullName() -> String? {
        return "\(givenName) \(middleName) \(familyName)"
    }
}
