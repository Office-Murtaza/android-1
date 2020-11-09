//
//  SendHelper.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 09.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation
import MessageUI

protocol SendHelperProtocol {
    func call(phoneNumber: String)
    func openWeblink(_ link: String)
}

extension SendHelperProtocol {
    func call(phoneNumber: String) {
        guard let phone = URL(string: "tel://\(phoneNumber)") else { return }
        UIApplication.shared.open(phone)
    }
    
    func openWeblink(_ link: String) {
        guard let url = URL(string: link) else { return }
        UIApplication.shared.open(url)
    }
}

