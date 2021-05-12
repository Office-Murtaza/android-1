//
//  KYCDataSource.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 13.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa

enum KYCItemType {
    case infoView(text: String?)
    case status(title: String, view: TransactionStatusView, status: String?, image: UIImage?)
    case kycDetails(leftTitle: String, leftData: String?, rightTitle: String, rightData: String?)
}

final class KYCDataSource: NSObject, HasDisposeBag, ItemsCountProvider {
    let kycRelay = PublishRelay<KYC?>()
    var cells: [KYCItemType] = []
    
    weak var tableView: UITableView? {
        didSet {
            guard let tableView = tableView else { return }
            tableView.register(KYCInfoView.self)
            tableView.register(KYCDetailsCell.self)
            tableView.register(KYCStatusCell.self)
            tableView.reloadData()
        }
    }
    
    private var value: KYC? {
        didSet {
            tableView?.reloadData()
        }
    }
    
    override init() {
        super.init()
        
        setupBindings()
    }
    
    private func setupBindings() {
        kycRelay
            .asDriver(onErrorDriveWith: .empty())
            .drive(onNext: { [weak self] in
                self?.value = $0
                self?.setupCells()
            })
            .disposed(by: disposeBag)
    }
    
    private func setupCells() {
        cells = []
        guard let status = value?.status else { return }
        var message: String?
        
        if value?.status == .verificationPending {
            message = localize(L.KYC.InfoView.verificationRejectedTitle)
        } else {
            message = value?.message
        }
        
        let infoViewCell = KYCItemType.infoView(text: message)
        
        let statusCell = KYCItemType.status(title: localize(L.KYC.Header.Status.title),
                                            view: TransactionStatusView(),
                                            status: status.verboseValue,
                                            image: status.associatedImage)
        let kycDetailsCell = KYCItemType.kycDetails(leftTitle: localize(L.KYC.Header.TransactionLimit.title),
                                                    leftData: "\(value?.txLimit ?? 0)",
                                                    rightTitle: localize(L.KYC.Header.DailyLimit.title),
                                                    rightData: "\(value?.dailyLimit ?? 0)")
        if message != nil {
            cells.append(infoViewCell)
        }
        cells.append(statusCell)
        cells.append(kycDetailsCell)
        tableView?.reloadData()
    }
}

extension KYCDataSource: UITableViewDataSource {
    func numberOfItems() -> Int {
        return cells.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cells.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch cells[indexPath.row] {
        case .infoView(let text):
            let cell = tableView.dequeueReusableCell(KYCInfoView.self, for: indexPath)
            cell.separatorInset = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: .greatestFiniteMagnitude)
            cell.setup(with: text)
            return cell
        case .status(let title, let view, let status, let image):
            let cell = tableView.dequeueReusableCell(KYCStatusCell.self, for: indexPath)
            view.configure(text: status, image: image)
            cell.configure(title: title, typeView: view)
            return cell
        case .kycDetails(let leftTitle, let leftData, let rightTitle, let rightData):
            let cell = tableView.dequeueReusableCell(KYCDetailsCell.self, for: indexPath)
            cell.configure(leftTitle: leftTitle, leftInfo: leftData, rightTitle: rightTitle, rightInfo: rightData)
            return cell
        }
    }
}

