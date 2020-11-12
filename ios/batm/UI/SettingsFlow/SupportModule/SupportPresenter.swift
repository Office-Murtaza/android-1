//
//  SupportPresenter.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 09.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxCocoa

class SupportPresenter: ModulePresenter, SupportModule, SendHelperProtocol {
    struct Input {
        var select: Driver<IndexPath>
    }
    
    let willSendEmailPressed = PublishRelay<String?>()
    let types = SupportCellType.allCases
    weak var delegate: SupportModuleDelegate?
    
    func bind(input: Input) {
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .subscribe(onNext: { [weak self] in
                switch $0 {
                case .phone: self?.call(phoneNumber: $0.link)
                case .email: self?.willSendEmailPressed.accept($0.link)
                case .telegram: self?.openWeblink($0.link)
                case .whatsApp: self?.openWeblink($0.link)
                }
            })
            .disposed(by: disposeBag)
    }
}
