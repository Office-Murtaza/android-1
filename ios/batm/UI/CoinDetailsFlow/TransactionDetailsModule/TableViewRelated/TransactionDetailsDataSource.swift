//
//  TransactionDetailsDataSource.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 17.03.2021.
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
    let transactionsRelay = PublishRelay<TransactionDetails?>()
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
    
    var coinType: CustomCoinType? {
        didSet {
            tableView?.reloadData()
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
                self?.setupCells(with: $0)
            })
            .disposed(by: disposeBag)
    }
    
    private func setupCells(with transactionDetails: TransactionDetails?) {
        guard let details = transactionDetails,
              let coinType = coinType,
              let transactionType = details.type else { return }
        
        let id = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.id),
                                                               data: details.txId ?? details.txDbId.toString(),
                                                               link: details.link)
        let amount = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.amount),
                                                                   data: "\(details.cryptoAmount?.formatted() ?? "") \(coinType.code)")
        let fee = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fee),
                                                                data: "\(details.cryptoFee?.formatted() ?? "") \(coinType.code)")
        
        let confirmations = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.confirmations),
                                                                          data: details.confirmations.toString())
        let fromAddress = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fromAddress),
                                                                        data: details.fromAddress)
        let toAddress = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.toAddress),
                                                                      data: details.toAddress)
        let date = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.date),
                                                                 data: details.timestamp?.timestampToStringDate(format: GlobalConstants.longDateForm))
        let fromPhone = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.fromPhone),
                                                                      data: details.fromPhone)
        let toPhone = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.toPhone),
                                                                    data: details.toPhone)
        let swapId = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.swapId),
                                                                   data: details.refTxId,
                                                                   link: details.refLink)
        let swapAmount = TransactionDetailsItemType
            .transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.swapAmount),
                                data: "\(details.refCryptoAmount?.formatted() ?? "") \(details.refCoin?.code ?? "")")
        let sellAmount = TransactionDetailsItemType.transactionDetails(title: localize(L.TransactionDetails.HeaderTitle.sellAmount),
                                                                       data: "$\(details.fiatAmount?.formatted() ?? "")")
        let type = TransactionDetailsItemType.type(title: localize(L.TransactionDetails.HeaderTitle.type),
                                                   view: TransactionTypeView(),
                                                   type: transactionType.verboseValue,
                                                   color: transactionType.associatedColor)
        let status = TransactionDetailsItemType.status(title: localize(L.TransactionDetails.HeaderTitle.status),
                                                       view: TransactionStatusView(),
                                                       status: details.status?.verboseValue,
                                                       image: details.status?.associatedImage)
        let cashStatus = TransactionDetailsItemType.status(title: localize(L.TransactionDetails.HeaderTitle.cashStatus),
                                                           view: TransactionStatusView(),
                                                           status: details.status?.verboseValue,
                                                           image: details.status?.associatedImage)
        let gif = TransactionDetailsItemType.gif
        let qrCode = TransactionDetailsItemType.qrCode
        
        if details.txId != nil || details.txDbId != nil {
            transactionCells.append(id)
        }
        
        transactionCells.append(type)
        transactionCells.append(status)
        
        if details.confirmations != nil {
            transactionCells.append(confirmations)
        }
        
        transactionCells.append(amount)
        
        if details.cryptoFee != nil, details.fromAddress != nil, details.toAddress != nil {
            transactionCells.append(fee)
            transactionCells.append(fromAddress)
            transactionCells.append(toAddress)
        }
        
        transactionCells.append(date)
        
        if details.fromPhone != nil, details.toPhone != nil {
            transactionCells.append(fromPhone)
            transactionCells.append(toPhone)
        }
        
        if details.image != nil, details.message != nil {
            transactionCells.append(gif)
        }
        
        if details.refTxId != nil, details.refLink != nil, details.refCoin != nil, details.refCryptoAmount != nil {
            transactionCells.append(swapId)
            transactionCells.append(swapAmount)
        }
        
        if details.fiatAmount != nil, details.cashStatus != nil, details.sellInfo != nil {
            transactionCells.append(sellAmount)
            transactionCells.append(cashStatus)
            transactionCells.append(qrCode)
        }
        
        value = transactionDetails
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
            cell.configure(image: value?.image, message: value?.message)
            return cell
        case .qrCode:
            let cell = tableView.dequeueReusableCell(QRCodeCell.self, for: indexPath)
            guard let qrCode = UIImage.qrCode(from: value?.sellInfo ?? "") else { return UITableViewCell() }
            cell.configure(qrCode: qrCode)
            return cell
        }
    }
}
