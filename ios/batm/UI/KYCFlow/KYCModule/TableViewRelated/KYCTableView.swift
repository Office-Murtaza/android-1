//
//  KYCTableView.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 13.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa

class KYCTableView: UITableView {
    lazy var kycInfoView = KYCInfoView()
    
    override init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = .white
        separatorInset = .zero
        separatorColor = .slateGrey
        tableFooterView = UIView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
