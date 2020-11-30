//
//  DealsFlow.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import RxFlow
import RxSwift

class DealsFlow: BaseFlow<BTMNavigationController, DealsFlowController> {
    override func assemblies() -> [Assembly] {
        return [Dependencies(),
                DealsAssembly()]
    }
    
    enum Steps: Step, Equatable {
        case deals
    }
    
    override func route(to step: Step) -> NextFlowItems {
        return castable(step)
            .map(handleFlow(step:))
            .extract(NextFlowItems.none)
    }
    
    private func handleFlow(step: Steps) -> NextFlowItems {
        switch step {
        case .deals:
            let module = resolver.resolve(Module<DealsModule>.self)!
            module.controller.title = localize(L.Deals.title)
            module.controller.tabBarItem.image = UIImage(named: "tab_bar_deals")
            module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_deals")
            return push(module.controller, animated: false)
        }
    }
}
