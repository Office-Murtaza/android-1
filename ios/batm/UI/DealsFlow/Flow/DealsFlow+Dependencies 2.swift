//
//  DealsFlow+Dependencies.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import Swinject

extension DealsFlow {
    class Dependencies: Assembly {
        func assemble(container: Container) {
            container
                .register(DealsFlowController.self) { ioc in
                    let flowController = DealsFlowController()
                    flowController.delegate = ioc.resolve(DealsFlowControllerDelegate.self)
                    return flowController
                }
                .inObjectScope(.container)
                .implements(DealsModuleDelegate.self)
        }
  }
}
