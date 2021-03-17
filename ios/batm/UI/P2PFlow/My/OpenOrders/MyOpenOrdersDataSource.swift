import UIKit

class MyOpenOrdersDataSource: NSObject, UITableViewDelegate, UITableViewDataSource {
    
    var viewModels = [MyOpenOrdersCellViewModel]()
    
    weak var tableView: UITableView?
    
    func udpate(vm: [MyOpenOrdersCellViewModel]) {
        viewModels = vm
        tableView?.reloadData()
    }

    func setup(tableView: UITableView?) {
        self.tableView = tableView
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.register(MyOpenOrdersCell.self, forCellReuseIdentifier: MyOpenOrdersCell.reuseIdentifier)
        tableView?.tableFooterView = UIView()
        tableView?.rowHeight = 136
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return viewModels.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: MyOpenOrdersCell.reuseIdentifier, for: indexPath) as? MyOpenOrdersCell else {
            return UITableViewCell()
        }
        let vm = viewModels[indexPath.row]
        cell.update(viewModel: vm)
        
        return cell
    }
    
}
