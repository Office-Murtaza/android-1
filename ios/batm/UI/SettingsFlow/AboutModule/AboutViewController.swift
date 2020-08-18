import UIKit
import RxSwift
import RxCocoa
import SnapKit

class AboutViewController: ModuleViewController<AboutPresenter> {
  
  let logoImageView = UIImageView(image: UIImage(named: "about_logo"))
  
  var dataSource: SettingsTableViewDataSource!
  
  let tableView = SettingsTableView()
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func viewWillAppear(_ animated: Bool) {
    if let index = self.tableView.indexPathForSelectedRow {
      self.tableView.deselectRow(at: index, animated: true)
    }
  }
  
  override func setupUI() {
    title = localize(L.About.title)
    view.backgroundColor = .white
    
    view.addSubviews(logoImageView,
                     tableView)
  }
  
  override func setupLayout() {
    logoImageView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide).offset(60)
      $0.centerX.equalToSuperview()
    }
    tableView.snp.makeConstraints {
      $0.top.equalTo(logoImageView.snp.bottom).offset(60)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    dataSource.values = presenter.types
    tableView.dataSource = dataSource
    dataSource.tableView = tableView
    
    tableView.rx.itemSelected.asDriver()
      .drive(onNext: { [tableView] in tableView.deselectRow(at: $0, animated: true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let selectDriver = tableView.rx.itemSelected.asDriver()
    
    presenter.bind(input: AboutPresenter.Input(select: selectDriver))
  }
  
}
