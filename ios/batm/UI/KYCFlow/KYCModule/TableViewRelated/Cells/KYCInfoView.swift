//
//  KYCInfoView.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 13.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

final class KYCInfoView: UITableViewCell {
    private lazy var containerView = UIView()
    private lazy var cellImageView = UIImageView(image: UIImage(named: "kyc_warning"))
        
    private lazy var label: UILabel = {
        let label = UILabel()
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
    
    func setup(with text: String?) {
        isHidden = text == nil
        label.attributedText = text?.htmlAttributedString(size: 16, color: .ceruleanBlue)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(containerView)
        
        contentView.backgroundColor = .white
        containerView.layer.cornerRadius = 4
        containerView.backgroundColor = .duckEggBlue

        containerView.addSubviews([cellImageView, label])
    }
    
    private func setupLayout() {
        containerView.snp.remakeConstraints {
            $0.leading.trailing.equalToSuperview()
            $0.top.equalToSuperview().offset(16)
            $0.bottom.equalToSuperview().offset(-16)
        }
        
        cellImageView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(20)
            $0.centerX.equalToSuperview()
            $0.width.height.equalTo(24)
        }
        
        label.snp.makeConstraints {
            $0.top.equalTo(cellImageView.snp.bottom).offset(10)
            $0.left.right.equalToSuperview().inset(12)
            $0.bottom.equalToSuperview().offset(-24)
        }
    }
}

extension String {
    func htmlAttributedString(size: CGFloat, color: UIColor) -> NSAttributedString? {
        let htmlTemplate = """
                <!doctype html>
                <html>
                  <head>
                    <style>
                      body {
                        color: \(color.hexString!);
                        font-family: -apple-system;
                        font-size: \(size)px;
                        text-align: center;
                      }
                    </style>
                  </head>
                  <body>
                    \(self)
                  </body>
                </html>
                """
        
        guard let data = htmlTemplate.data(using: .utf8) else { return nil }
        
        guard let attributedString = try? NSAttributedString(data: data,
                                                             options: [.documentType: NSAttributedString.DocumentType.html],
                                                             documentAttributes: nil) else { return nil }
        
        return attributedString
    }
}

extension UIColor {
    var hexString:String? {
        if let components = self.cgColor.components {
            let r = components[0]
            let g = components[1]
            let b = components[2]
            return  String(format: "#%02x%02x%02x", (Int)(r * 255), (Int)(g * 255), (Int)(b * 255))
        }
        return nil
    }
}
