//
//  NotificationsViewController.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit

class NotificationsViewController: ModuleViewController<NotificationsPresenter> {
    var dataSource: NotificationsTableViewDataSource!
    let tableView = NotificationsTableView()
    
    override func setupUI() {
        title = localize(L.Notifications.title)
        view.addSubviews(tableView)
    }
    
    override func setupLayout() {
        tableView.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide)
            $0.left.right.bottom.equalToSuperview()
        }
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let changeVisibilityDriver = dataSource.changeVisibilityRelay.asDriver(onErrorDriveWith: .empty())
        
        presenter.bind(input: NotificationsPresenter.Input(changeVisibility: changeVisibilityDriver))
    }
    
    private func setupUIBindings() {
        tableView.dataSource = dataSource
        dataSource.tableView = tableView
    }
}
