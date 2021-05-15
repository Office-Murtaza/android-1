import UIKit
import CoreLocation

protocol MyOpenOrdersDataSourceDelegate: AnyObject {
    func didSelected(vm: MyOpenOrdersCellViewModel)
}

class MyOpenOrdersDataSource: NSObject, UITableViewDelegate, UITableViewDataSource {
    
    var viewModels = [MyOpenOrdersCellViewModel]()
    weak var delegate: MyOpenOrdersDataSourceDelegate?
    
    weak var tableView: UITableView?
    
    func udpate(vm: [MyOpenOrdersCellViewModel]) {
        viewModels = vm
        tableView?.reloadData()
    }
    
    func reload(location: CLLocation?) {
        DispatchQueue.global(qos: .background).async { [weak self] in
            self?.viewModels.forEach{ $0.update(location: location) }
            DispatchQueue.main.async { [weak self] in
                self?.tableView?.reloadData()
            }
        }
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
        cell.selectionStyle = .none
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let vm = viewModels[indexPath.row]
        delegate?.didSelected(vm: vm)
    }
    
}
