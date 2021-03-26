//
//  QRCodeCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

final class QRCodeCell: UITableViewCell {    
    private lazy var qrCodeImageView = UIImageView()
    
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
        qrCodeImageView.image = nil
    }
    
    func configure(qrCode: UIImage) {
        qrCodeImageView.image = qrCode
    }
    
    private func setupUI() {
        isUserInteractionEnabled = false
        backgroundColor = .white
        contentView.addSubview(qrCodeImageView)
    }
    
    private func setupLayout() {
        qrCodeImageView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(24)
            $0.bottom.equalToSuperview().offset(-16)
            $0.width.height.equalTo(240)
            $0.centerX.equalTo(contentView.snp.centerX)
        }
    }
}
