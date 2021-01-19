import UIKit
import Contacts
import RxSwift
import RxCocoa

class TransferTableDataSource: NSObject, UITableViewDelegate, UITableViewDataSource {
    
    weak var tableView: UITableView?

    var contacts = [CGroup]()
    
    var selectedContact = BehaviorRelay<BContact?>(value: nil)
    
    func configure(tableView: UITableView) {
        self.tableView = tableView
        tableView.register(TransferContactCell.self, forCellReuseIdentifier: "TransferContactCell")
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 70
        tableView.separatorStyle = .none
        tableView.keyboardDismissMode = .onDrag
    }
    
    func reloadContacts(_ contacts: [CGroup]) {
        self.contacts = contacts
        tableView?.reloadData()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return contacts[section].filteredContacts?.count ?? 0
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return contacts.count
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let title = contacts[section].key
        let view = TransferSectionView()
        view.title.text = title
        return view
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: "TransferContactCell", for: indexPath) as? TransferContactCell else { return UITableViewCell() }
        let contactSection = contacts[indexPath.section]
        guard let fContacts = contactSection.filteredContacts else { return UITableViewCell()}
        let contact = fContacts[indexPath.row]
        cell.contactView.update(contact: contact)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let filteredContact = contacts[indexPath.section].filteredContacts else { return }
        let contact = filteredContact[indexPath.row]
        selectedContact.accept(contact)
    }
}


