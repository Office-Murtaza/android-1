//
//  TransactionDetailsView.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

class TransactionTypeView: UIView {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .bold)
        return label
    }()
    
    private lazy var contentView = UIView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(text: String, textColor: UIColor, backgroundColor: UIColor) {
        titleLabel.text = text
        titleLabel.textColor = textColor
        contentView.backgroundColor = backgroundColor
    }
    
    private func setupUI() {
        addSubview(contentView)
        
        contentView.layer.cornerRadius = 12
        contentView.addSubview(titleLabel)
    }
    
    private func setupLayout() {
        contentView.snp.makeConstraints {
            $0.width.greaterThanOrEqualTo(47)
            $0.edges.equalToSuperview()
        }
        titleLabel.snp.makeConstraints {
            $0.centerY.equalTo(contentView.snp.centerY)
            $0.left.equalTo(contentView.snp.left).offset(12)
            $0.right.equalTo(contentView.snp.right).offset(-12)
        }
    }
}
