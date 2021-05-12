//
//  KYCDetailsCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 13.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

final class KYCDetailsCell: UITableViewCell {
    private lazy var leftStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 2
        return stackView
    }()
    
    private lazy var rightStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 2
        return stackView
    }()
    
    private lazy var leftTitleLabel: UILabel = {
        return setupLabel(size: 12, textColor: .slateGrey, alignment: .left)
    }()
    
    private lazy var leftInfoLabel: UILabel = {
        return setupLabel(size: 14, weight: .semibold, textColor: .darkText, alignment: .left)
    }()
    
    private lazy var rightTitleLabel: UILabel = {
        return setupLabel(size: 12, textColor: .slateGrey, alignment: .right)
    }()
    
    private lazy var rightInfoLabel: UILabel = {
        return setupLabel(size: 14, weight: .semibold, textColor: .darkText, alignment: .right)
    }()
    
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
        leftTitleLabel.text = nil
        leftInfoLabel.text = nil
        rightTitleLabel.text = nil
        rightInfoLabel.text = nil
    }
    
    func configure(leftTitle: String, leftInfo: String?, rightTitle: String, rightInfo: String?) {
        leftTitleLabel.text = leftTitle
        leftInfoLabel.text = "$\(leftInfo ?? "")"
        rightTitleLabel.text = rightTitle
        rightInfoLabel.text = "$\(rightInfo ?? "")"
    }
    
    private func setupUI() {
        isUserInteractionEnabled = false
        backgroundColor = .white
        
        contentView.addSubviews([leftStackView, rightStackView])
        leftStackView.addArrangedSubviews([leftTitleLabel, leftInfoLabel])
        rightStackView.addArrangedSubviews([rightTitleLabel, rightInfoLabel])
    }
    
    private func setupLayout() {
        contentView.snp.makeConstraints {
            $0.edges.equalToSuperview()
            $0.height.equalTo(75)
        }
        
        leftStackView.snp.makeConstraints {
            $0.left.equalToSuperview()
            $0.right.equalTo(rightStackView.snp.left)
            $0.top.equalToSuperview().offset(16)
            $0.bottom.equalToSuperview().offset(-16)
            $0.width.equalTo(rightStackView.snp.width)
        }
        
        rightStackView.snp.makeConstraints {
            $0.left.equalTo(leftStackView.snp.right)
            $0.right.equalToSuperview()
            $0.top.equalToSuperview().offset(16)
            $0.bottom.equalToSuperview().offset(-16)
            $0.width.equalTo(leftStackView.snp.width)
        }
    }
    
    private func setupLabel(size: CGFloat, weight: UIFont.Weight = .regular, textColor: UIColor, alignment: NSTextAlignment) -> UILabel {
        let label = UILabel()
        label.font = .systemFont(ofSize: size, weight: weight)
        label.textColor = textColor
        label.numberOfLines = 1
        label.textAlignment = alignment
        return label
    }
}
