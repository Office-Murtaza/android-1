//
//  NotificationsCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

protocol NotificationsCellDelegate: class {
    func didTapChangeNotifications()
}

final class NotificationsCell: UITableViewCell {
    var disposeBag = DisposeBag()
    weak var delegate: NotificationsCellDelegate?
    
    let typeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .medium)
        label.textColor = .slateGrey
        return label
    }()
    
    let visibilitySwitch: UISwitch = {
        let toggle = UISwitch()
        toggle.onTintColor = .ceruleanBlue
        return toggle
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
        typeLabel.text = nil
        disposeBag = DisposeBag()
    }
    
    func configure() {
        typeLabel.text = localize(L.Notifications.Cell.title)
        visibilitySwitch.rx.isOn
          .asDriver()
          .drive(onNext: { [weak self] _ in self?.delegate?.didTapChangeNotifications() })
          .disposed(by: disposeBag)
    }
    
    private func setupUI() {
        contentView.addSubviews(typeLabel,
                                visibilitySwitch)
    }
    
    private func setupLayout() {
        typeLabel.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.right.lessThanOrEqualTo(visibilitySwitch.snp.left).offset(-15)
            $0.centerY.equalToSuperview()
        }
        visibilitySwitch.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-15)
            $0.centerY.equalToSuperview()
        }
    }
}
