//
//  DealsUsecase.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift

protocol DealsUsecase {}

class DealsUsecaseImpl: DealsUsecase {
    let api: APIGateway
    
    init(api: APIGateway) {
        self.api = api
    }
}
