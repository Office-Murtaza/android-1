//
//  SwapPlatformFeeView.swift
//  batm
//
//  Created by Dmytro Frolov on 12.12.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit

class SwapPlatformFeeView: UIView {
    
    private lazy var feeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    private lazy var feeTitleLabel: UILabel = {
        let label = UILabel()
        label.text = localize(L.Swap.platformfee)
        label.textColor = .slateGrey
        label.font = .systemFont(ofSize: 16, weight: .regular)
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        
        addSubviews([feeLabel, feeTitleLabel])
    }
    
    private func setupLayout() {
        feeTitleLabel.snp.makeConstraints{
            $0.top.bottom.equalToSuperview()
            $0.left.equalToSuperview().offset(15)
            $0.right.equalTo(self.snp.centerX)
        }
        feeLabel.snp.makeConstraints{
            $0.right.equalToSuperview().offset(-15)
            $0.top.bottom.equalToSuperview()
            $0.left.greaterThanOrEqualTo(feeTitleLabel.snp.right)
        }
    }
    
    func configure(fee: String) {
        feeLabel.text = fee
    }

}
