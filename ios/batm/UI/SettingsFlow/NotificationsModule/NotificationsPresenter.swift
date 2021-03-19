//
//  NotificationsPresenter.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxCocoa

class NotificationsPresenter: ModulePresenter, NotificationsModule {
    struct Input {
        var changeVisibility: Driver<Bool>
    }
    weak var delegate: NotificationsModuleDelegate?
    
    func bind(input: Input) {
        input.changeVisibility
            .asObservable()
            .subscribe(onNext: { value in
                if value {
                    UIApplication.shared.registerForRemoteNotifications()
                } else {
                    UIApplication.shared.unregisterForRemoteNotifications()
                }
            })
            .disposed(by: disposeBag)
    }
}
