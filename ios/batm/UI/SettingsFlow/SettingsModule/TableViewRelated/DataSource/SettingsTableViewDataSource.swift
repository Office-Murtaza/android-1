import UIKit
import RxSwift
import RxCocoa

final class SettingsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
  
  var values: [SettingsCellTypeRepresentable] = [] {
    didSet {
      tableView?.reloadData()
    }
  }
  
  weak var tableView: UITableView? {
    didSet {
      guard let tableView = tableView else { return }
      tableView.register(SettingsCell.self)
      tableView.reloadData()
    }
  }
  
  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    values.count
  }
  
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let model = values[indexPath.item]
    let cell = tableView.dequeueReusableCell(SettingsCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
}
