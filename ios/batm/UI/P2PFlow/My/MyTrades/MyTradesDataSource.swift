import UIKit
import SnapKit

class MyTradesDataSource: NSObject, UITableViewDelegate, UITableViewDataSource {
    
    var viewModels = [MyTradesCellViewModel]()
    
    weak var tableView: UITableView?
    
    func udpate(vm: [MyTradesCellViewModel]) {
        viewModels = vm
        tableView?.reloadData()
    }

    func setup(tableView: UITableView?) {
        self.tableView = tableView
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.register(MyTradesCell.self, forCellReuseIdentifier: MyTradesCell.reuseIdentifier)
        tableView?.tableFooterView = UIView()
        tableView?.rowHeight = 85
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return viewModels.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: MyTradesCell.reuseIdentifier, for: indexPath) as? MyTradesCell else {
            return UITableViewCell()
        }
        let vm = viewModels[indexPath.row]
        cell.update(viewModel: vm)
        
        return cell
    }
    
}
