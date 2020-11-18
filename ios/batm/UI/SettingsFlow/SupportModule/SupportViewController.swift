//
//  SupportViewController.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 09.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import MessageUI

class SupportViewController: ModuleViewController<SupportPresenter> {
    var dataSource: SettingsTableViewDataSource!
    let tableView = SettingsTableView()
    
    override func viewWillAppear(_ animated: Bool) {
        if let index = self.tableView.indexPathForSelectedRow {
            self.tableView.deselectRow(at: index, animated: true)
        }
    }
    
    override func setupUI() {
        title = localize(L.Support.title)
        view.backgroundColor = .white
        
        view.addSubviews(tableView)
    }
    
    override func setupLayout() {
        tableView.snp.makeConstraints {
            $0.left.right.top.bottom.equalToSuperview()
        }
    }
    
    private func setupUIBindings() {
        dataSource.values = presenter.types
        tableView.dataSource = dataSource
        dataSource.tableView = tableView
        
        tableView.rx.itemSelected.asDriver()
            .drive(onNext: { [weak self] in self?.tableView.deselectRow(at: $0, animated: true) })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let selectDriver = tableView.rx.itemSelected.asDriver()
        
        presenter.bind(input: SupportPresenter.Input(select: selectDriver))
        presenter.willSendEmailPressed.asObservable().subscribe { [weak self] email in
            self?.sendEmail(to: email ?? "")
        }.disposed(by: disposeBag)
    }
}

extension SupportViewController: MFMailComposeViewControllerDelegate {
    func sendEmail(to email: String) {
        if MFMailComposeViewController.canSendMail() {
            let composeVC = MFMailComposeViewController()
            composeVC.mailComposeDelegate = self
            composeVC.setToRecipients([email])
            
            present(composeVC, animated: true, completion: nil)
        }
    }
    
    func mailComposeController(_ controller: MFMailComposeViewController,
                               didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
}
