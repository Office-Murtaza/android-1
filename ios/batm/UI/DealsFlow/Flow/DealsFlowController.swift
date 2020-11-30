//
//  DealsFlowController.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxFlow

protocol DealsFlowControllerDelegate: class {}

class DealsFlowController: FlowController, FlowActivator {
    var initialStep: Step = DealsFlow.Steps.deals
    weak var delegate: DealsFlowControllerDelegate?
}

extension DealsFlowController: ATMModuleDelegate {}
