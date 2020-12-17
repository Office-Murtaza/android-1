import UIKit
import RxSwift
import RxCocoa

final class DealsTableViewDataSource: NSObject, UITableViewDataSource, HasDisposeBag {
    var values: [DealsCellTypeRepresentable] = [] {
        didSet {
            tableView?.reloadData()
        }
    }
    
    weak var tableView: UITableView? {
        didSet {
            guard let tableView = tableView else { return }
            tableView.register(DealsCell.self)
            tableView.reloadData()
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        values.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let model = values[indexPath.item]
        let cell = tableView.dequeueReusableCell(DealsCell.self, for: indexPath)
        cell.configure(for: model)
        return cell
    }
}
