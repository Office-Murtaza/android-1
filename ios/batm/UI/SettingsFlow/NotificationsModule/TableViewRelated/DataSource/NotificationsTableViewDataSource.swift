import UIKit
import RxSwift
import RxCocoa

final class NotificationsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
    let changeVisibilityRelay = PublishRelay<Bool>()
    
    weak var tableView: UITableView? {
        didSet {
            guard let tableView = tableView else { return }
            tableView.register(NotificationsCell.self)
            tableView.reloadData()
        }
    }
    
    override init() {
        super.init()
        
        setupBindings()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(NotificationsCell.self, for: indexPath)
        cell.delegate = self
        cell.configure()
        return cell
    }
    
    private func setupBindings() {}
}

extension NotificationsTableViewDataSource: NotificationsCellDelegate {
    func didTapChangeNotifications() {
        UserDefaultsHelper.notificationsEnabled = !UserDefaultsHelper.notificationsEnabled.value
        changeVisibilityRelay.accept(UserDefaultsHelper.notificationsEnabled.value)
    }
}
