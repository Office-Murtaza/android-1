import UIKit
import RxSwift
import RxCocoa

final class SecurityTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
  
  var values: [SecurityCellType] = [] {
    didSet {
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(SettingsCell.self)
      tableView.register(SecurityLocalAuthCell.self)
      tableView.reloadData()
    }
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    switch model {
    case .faceId: fallthrough
    case .touchId:
        let localAuthCell = tableView.dequeueReusableCell(SecurityLocalAuthCell.self, for: indexPath)
        localAuthCell.delegate = self
        localAuthCell.configure(for: model)
        return localAuthCell
    default:
        let cell = tableView.dequeueReusableCell(SettingsCell.self, for: indexPath)
        cell.configure(for: model)
        return cell
    }
  }
}

extension SecurityTableViewDataSource: SecurityLocalAuthCellDelegate {
    func didTapChangeLocalAuthCell() {
        UserDefaultsHelper.isLocalAuthEnabled = !UserDefaultsHelper.isLocalAuthEnabled
    }
}
