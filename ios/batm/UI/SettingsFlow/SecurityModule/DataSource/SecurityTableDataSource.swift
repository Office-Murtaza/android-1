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
    if case .faceId = model {
        let faceIdCell = tableView.dequeueReusableCell(SecurityLocalAuthCell.self, for: indexPath)
        faceIdCell.delegate = self
        faceIdCell.configure(for: model)
        return faceIdCell
    } else {
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
