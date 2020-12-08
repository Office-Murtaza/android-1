//
//  NotificationsPresenter.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxCocoa

class NotificationsPresenter: ModulePresenter, NotificationsModule {
    struct Input {
        var changeVisibility: Driver<Void>
    }
    weak var delegate: NotificationsModuleDelegate?
    
    func bind(input: Input) {}
}
