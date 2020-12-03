//
//  SwapExchangeRateView.swift
//  batm
//
//  Created by Dmytro Frolov on 02.12.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit

class SwapExchangeRateView: UIView {
    
    private let trandingView = UIButton(type: .system)
    private let tildeLabel = UILabel()

    private lazy var fromCoinLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        return label
    }()

    private lazy var toCoinLabel:UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        return label
    }()

    lazy var stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.layoutMargins = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
        stack.isLayoutMarginsRelativeArrangement = true
        return stack
    }()
    
    override init(frame: CGRect){
        super.init(frame: frame)
        setupUI()
        setupLayout()
        tildeLabel.text = localize(L.Swap.tilda)
        trandingView.setImage(UIImage(named: "trending_up"), for: .normal)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(fromCoin: String, toCoin: String) {
        fromCoinLabel.text = fromCoin
        toCoinLabel.text = toCoin
    }
    
    private func setupUI() {
        addSubview(stackView)
        stackView.addArrangedSubviews([
            trandingView,
            fromCoinLabel,
            tildeLabel,
            toCoinLabel,
        ])
    
        backgroundColor = .white
        layer.borderWidth = 2
        layer.borderColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12).cgColor
        layer.masksToBounds = true
        layer.cornerRadius = 18
    }
    
    private func setupLayout() {
        stackView.snp.remakeConstraints{
            $0.edges.equalToSuperview()
        }
        trandingView.snp.makeConstraints {
            $0.width.height.equalTo(36)
        }
        
    }
 
}
