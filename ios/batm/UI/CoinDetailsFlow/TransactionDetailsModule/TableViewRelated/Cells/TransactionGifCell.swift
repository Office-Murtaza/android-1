//
//  GifCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa
import GiphyUISDK
import GiphyCoreSDK

final class TransactionGifCell: UITableViewCell {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.spacing = 16
        return stackView
    }()
    
    private lazy var gifContainer: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 4
        view.layer.masksToBounds = true
        return view
    }()
    
    private lazy var gifMediaView: GPHMediaView = {
        let view = GPHMediaView()
        view.isHidden = true
        return view
    }()
    
    private lazy var infoLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.textColor = .slateGrey
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
        infoLabel.text = nil
    }
    
    func configure(imageId: String?, message: String?) {
        guard let imageId = imageId else { return }
        
        GiphyCore.shared.gifByID(imageId) { response, error in
            guard let media = response?.data else { return }
            DispatchQueue.main.async { [weak self] in
                guard let self = self else { return }
                self.gifMediaView.isHidden = false
                self.gifMediaView.setMedia(media, rendition: .fixedHeightSmall)
            }
        }
        
        infoLabel.text = message
    }
    
    private func setupUI() {
        isUserInteractionEnabled = false
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = .white
        
        contentView.addSubview(stackView)
        stackView.addArrangedSubviews([gifContainer, infoLabel])
        gifContainer.addSubviews(gifMediaView)
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.left.right.equalToSuperview()
            $0.top.equalToSuperview().offset(16)
            $0.bottom.equalToSuperview().offset(-16)
        }
        
        gifContainer.snp.makeConstraints {
            $0.width.equalTo(130)
            $0.height.equalTo(72)
        }
        
        gifMediaView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
}
