//
//  TransactionDetailsCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

final class TransactionDetailsCell: UITableViewCell {
    var isLink: Bool = false
    
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 2
        return stackView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = .slateGrey
        label.numberOfLines = 0
        return label
    }()
    
    private lazy var infoLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.textColor = isLink ? .systemBlue : .darkText
        label.numberOfLines = 0
        return label
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
        titleLabel.text = nil
        infoLabel.text = nil
    }
    
    func configure(title: String, info: String?, link: String?) {
        isUserInteractionEnabled = link != nil
        stackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
        
        if let link = link, let linkURL = URL(string: link), let info = info {
            let linkView = LinkView()
            linkView.configure(text: info, link: linkURL)
            contentView.addSubview(linkView)
            stackView.addArrangedSubviews([titleLabel, linkView])
        } else {
            stackView.addArrangedSubviews([titleLabel, infoLabel])
        }
        
        titleLabel.text = title
        infoLabel.text = info
    }
    
    private func setupUI() {
        backgroundColor = .white
        
        contentView.addSubview(stackView)
        stackView.addArrangedSubviews([titleLabel, infoLabel])
    }
    
    private func setupLayout() {
        contentView.snp.makeConstraints {
            $0.edges.equalToSuperview()
            $0.height.greaterThanOrEqualTo(71)
        }
        stackView.snp.makeConstraints {
            $0.left.right.equalToSuperview()
            $0.top.equalToSuperview().offset(16)
            $0.bottom.equalToSuperview().offset(-16)
        }
    }
}
