//
//  NotificationsAssembly.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import Swinject

class NotificationsAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<NotificationsModule>.self) { resolver in
            let dataSource = NotificationsTableViewDataSource()
            let viewController = NotificationsViewController()
            let presenter = NotificationsPresenter()
            
            presenter.delegate = resolver.resolve(NotificationsModuleDelegate.self)
            viewController.presenter = presenter
            viewController.dataSource = dataSource
            
            return Module<NotificationsModule>(controller: viewController, input: presenter)
        }
    }
}
