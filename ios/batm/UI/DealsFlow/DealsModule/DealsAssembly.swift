//
//  DealsAssembly.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import Swinject

class DealsAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<DealsModule>.self) { resolver in
            let viewController = DealsViewController()
            let usecase = resolver.resolve(DealsUsecase.self)!
            let presenter = DealsPresenter(usecase: usecase)
            
            presenter.delegate = resolver.resolve(DealsModuleDelegate.self)
            viewController.presenter = presenter
            
            return Module<DealsModule>(controller: viewController, input: presenter)
        }
    }
}
