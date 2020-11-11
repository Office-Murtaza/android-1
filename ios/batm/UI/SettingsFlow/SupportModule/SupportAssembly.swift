//
//  SupportAssembly.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 09.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import Swinject
import UIKit

final class SupportAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<SupportModule>.self) { resolver in
            let dataSource = SettingsTableViewDataSource()
            let viewController = SupportViewController()
            let presenter = SupportPresenter()
            
            presenter.delegate = resolver.resolve(SupportModuleDelegate.self)
            viewController.presenter = presenter
            viewController.dataSource = dataSource
            return Module<SupportModule>(controller: viewController, input: presenter)
        }
    }
}
