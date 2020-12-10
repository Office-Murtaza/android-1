//
//  NotificationsTableView.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa

class NotificationsTableView: UITableView {
    override init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = .white
        rowHeight = 65
        separatorInset = .zero
        tableHeaderView = UIView()
        tableFooterView = UIView()
        allowsSelection = false
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
