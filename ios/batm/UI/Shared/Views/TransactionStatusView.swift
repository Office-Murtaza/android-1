//
//  TransactionStatusView.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

class TransactionStatusView: UIView {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.spacing = 4
        stackView.addArrangedSubviews([statusLabel, statusImage])
        return stackView
    }()
    
    private lazy var statusLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        label.textColor = .darkText
        return label
    }()
    
    private lazy var statusImage = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(text: String?, image: UIImage?) {
        statusLabel.text = text
        statusImage.image = image
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.centerY.equalTo(snp.centerY)
            $0.left.equalToSuperview().offset(5)
            $0.right.equalToSuperview().offset(-5)
        }
        statusImage.snp.makeConstraints {
            $0.width.height.equalTo(16)
        }
    }
}

