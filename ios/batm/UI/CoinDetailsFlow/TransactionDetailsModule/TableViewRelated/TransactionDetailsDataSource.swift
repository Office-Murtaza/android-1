//
//  TransactionDetailsDataSource.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa

enum TransactionDetailsItemType {
    case type(title: String, view: TransactionTypeView, type: String, color: TransactionTypeColorPalette.Type)
    case status(title: String, view: TransactionStatusView, status: String?, image: UIImage?)
    case transactionDetails(title: String, data: String?, link: String? = nil)
    case gif
    case qrCode
}

final class TransactionDetailsDataSource: NSObject, HasDisposeBag, ItemsCountProvider {
    let transactionsRelay = BehaviorRelay<TransactionDetails?>(value: nil)
    var transactionCells: [TransactionDetailsItemType] = []
    
    weak var tableView: UITableView? {
        didSet {
            guard let tableView = tableView else { return }
            tableView.register(TransactionDetailsCell.self)
            tableView.register(TypeStatusCell.self)
            tableView.register(TransactionGifCell.self)
            tableView.register(QRCodeCell.self)
            tableView.reloadData()
        }
    }
    
    private var value: TransactionDetails? {
        didSet {
            tableView?.reloadData()
        }
    }
    
    override init() {
        super.init()
        
        setupBindings()
    }
    
    private func setupBindings() {
        transactionsRelay
            .asDriver(onErrorDriveWith: .empty())
            .drive(onNext: { [weak self] in
                self?.value = $0
                self?.setupCells()
            })
            .disposed(by: disposeBag)
    }
    
    private func setupCells() {
        guard let value = value, let transactionType = value.type else { return }
        
        let id = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.id),
                                                               data: value.txId ?? value.txDbId.toString(),
                                                               link: value.link)
        let amount = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.amount),
                                                                   data: value.cryptoAmount?.formatted())
        let fee = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fee),
                                                                data: value.cryptoFee?.formatted())
        let confirmations = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.confirmations),
                                                                          data: value.confirmations.toString())
        let fromAddress = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fromAddress),
                                                                        data: value.fromAddress)
        let toAddress = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.toAddress),
                                                                      data: value.toAddress)
        let date = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.date),
                                                                 data: value.date)
        let fromPhone = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fromPhone),
                                                                      data: value.fromPhone)
        let toPhone = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.toPhone),
                                                                    data: value.toPhone)
        let swapId = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.swapId),
                                                                   data: value.swapTxId,
                                                                   link: value.swapLink)
        let swapAmount = TransactionDetailsItemType
            .transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.swapAmount),
                                data: "\(value.swapCryptoAmount?.formatted() ?? "") \(value.swapCoin?.code ?? "")")
        let sellAmount = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.sellAmount),
                                                                       data: "$\(value.fiatAmount?.formatted() ?? "")")
        let type = TransactionDetailsItemType.type(title: localize(L.TransactionDetails.HeaderTitle.type),
                                                   view: TransactionTypeView(),
                                                   type: transactionType.verboseValue,
                                                   color: transactionType.associatedColor)
        let status = TransactionDetailsItemType.status(title: localize(L.TransactionDetails.HeaderTitle.status),
                                                       view: TransactionStatusView(),
                                                       status: value.status?.verboseValue,
                                                       image: value.status?.associatedImage)
        let cashStatus = TransactionDetailsItemType.status(title: localize(L.TransactionDetails.HeaderTitle.cashStatus),
                                                           view: TransactionStatusView(),
                                                           status: value.status?.verboseValue,
                                                           image: value.status?.associatedImage)
        let gif = TransactionDetailsItemType.gif
        let qrCode = TransactionDetailsItemType.qrCode
        
        if value.txId != nil || value.txDbId != nil {
            transactionCells.append(id)
        }
        
        transactionCells.append(type)
        transactionCells.append(status)
        
        if value.confirmations != nil {
            transactionCells.append(confirmations)
        }
        
        transactionCells.append(amount)
        
        if value.cryptoFee != nil, value.fromAddress != nil, value.toAddress != nil {
            transactionCells.append(fee)
            transactionCells.append(fromAddress)
            transactionCells.append(toAddress)
        }
        
        transactionCells.append(date)
        
        if value.fromPhone != nil, value.toPhone != nil {
            transactionCells.append(fromPhone)
            transactionCells.append(toPhone)
        }
        
        if value.imageId != nil, value.message != nil {
            transactionCells.append(gif)
        }
        
        if value.swapTxId != nil, value.swapLink != nil, value.swapCoin != nil, value.swapCryptoAmount != nil {
            transactionCells.append(swapId)
            transactionCells.append(swapAmount)
        }
        
        if value.fiatAmount != nil, value.cashStatus != nil, value.sellInfo != nil {
            transactionCells.append(sellAmount)
            transactionCells.append(cashStatus)
            transactionCells.append(qrCode)
        }
    }
}

extension TransactionDetailsDataSource: UITableViewDataSource {
    func numberOfItems() -> Int {
        return transactionCells.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return transactionCells.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch transactionCells[indexPath.row] {
        case .transactionDetails(let title, let data, let link):
            let cell = tableView.dequeueReusableCell(TransactionDetailsCell.self, for: indexPath)
            cell.configure(title: title, info: data, link: link)
            return cell
        case .type(let title, let view, let type, let color):
            let cell = tableView.dequeueReusableCell(TypeStatusCell.self, for: indexPath)
            view.configure(text: type, textColor: color.font, backgroundColor: color.background)
            cell.configure(title: title, typeView: view)
            return cell
        case .status(let title, let view, let status, let image):
            let cell = tableView.dequeueReusableCell(TypeStatusCell.self, for: indexPath)
            view.configure(text: status, image: image)
            cell.configure(title: title, typeView: view)
            return cell
        case .gif:
            let cell = tableView.dequeueReusableCell(TransactionGifCell.self, for: indexPath)
            cell.configure(imageId: value?.imageId, message: value?.message)
            return cell
        case .qrCode:
            let cell = tableView.dequeueReusableCell(QRCodeCell.self, for: indexPath)
            guard let qrCode = UIImage.qrCode(from: value?.sellInfo ?? "") else { return UITableViewCell() }
            cell.configure(qrCode: qrCode)
            return cell
        }
    }
}
