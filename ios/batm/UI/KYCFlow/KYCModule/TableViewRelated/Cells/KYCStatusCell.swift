//
//  KYCStatusCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 13.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

final class KYCStatusCell: UITableViewCell {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.textColor = .slateGrey
        return label
    }()
    
    private lazy var containerView = UIView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func prepareForReuse() {
        super.prepareForReuse()
        containerView.subviews.forEach { $0.removeFromSuperview() }
        titleLabel.text = nil
    }
    
    func configure(title: String, typeView: UIView) {
        titleLabel.text = title
        containerView.addSubview(typeView)
        typeView.snp.remakeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    private func setupUI() {
        isUserInteractionEnabled = false
        backgroundColor = .white
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(containerView)
    }
    
    private func setupLayout() {
        contentView.snp.makeConstraints {
            $0.edges.equalToSuperview()
            $0.height.equalTo(56)
        }
        
        titleLabel.snp.makeConstraints {
            $0.left.equalToSuperview()
            $0.centerY.equalTo(contentView.snp.centerY)
        }
        
        containerView.snp.makeConstraints {
            $0.left.greaterThanOrEqualTo(titleLabel.snp.right).offset(16)
            $0.right.equalToSuperview()
            $0.height.equalTo(24)
            $0.centerY.equalTo(contentView.snp.centerY)
        }
    }
}
