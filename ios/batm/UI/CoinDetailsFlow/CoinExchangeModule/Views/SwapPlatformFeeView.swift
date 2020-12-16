//
//  SwapPlatformFeeView.swift
//  batm
//
//  Created by Dmytro Frolov on 12.12.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit

class SwapPlatformFeeView: UIView {
    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.layoutMargins = UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 15)
        stack.isLayoutMarginsRelativeArrangement = true
        return stack
    }()

    private lazy var feeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .bold)
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
        stackView.addArrangedSubviews([
            feeTitleLabel,
            feeLabel
        ])
        
        addSubview(stackView)
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    func configure(fee: String) {
        feeLabel.text = fee
    }

}
