//
//  DealsPresenter.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxCocoa

class DealsPresenter: ModulePresenter, DealsModule {
    typealias Store = ViewStore<DealsAction, DealsState>
    
    weak var delegate: DealsModuleDelegate?

    private let usecase: DealsUsecase
    private let store: Store
    
    init(usecase: DealsUsecase,
         store: Store = DealsStore()) {
        self.usecase = usecase
        self.store = store
    }
}
