//
//  SupportCell.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 09.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import UIKit

enum SupportCellType: CaseIterable, SettingsCellTypeRepresentable {
    enum Const {
        enum Naming {
            static let phone = "+1 (888) 848-3033"
            static let email = "support@belcobtm.com"
            static let telegram = "@BelcoBTM"
            static let whatsApp = "HLM8HlzE5VjDjhEiZJpKJr"
        }
        
        enum Link {
            static let phone = "+18888483033"
            static let email = "support@belcobtm.com"
            static let telegram = "https://t.me/BelcoBTM"
            static let whatsApp = "https://chat.whatsapp.com/HLM8HlzE5VjDjhEiZJpKJr"
        }
    }
    
    case phone
    case email
    case telegram
    case whatsApp
    
    var title: String {
        switch self {
        case .phone: return localize(L.Support.Cell.phone)
        case .email: return localize(L.Support.Cell.email)
        case .telegram: return localize(L.Support.Cell.telegram)
        case .whatsApp: return localize(L.Support.Cell.whatsApp)
        }
    }
    
    var value: String? {
        switch self {
        case .phone: return Const.Naming.phone
        case .email: return Const.Naming.email
        case .telegram: return Const.Naming.telegram
        case .whatsApp: return Const.Naming.whatsApp
        }
    }
    
    var image: UIImage? {
        switch self {
        case .phone: return UIImage(named: "support_phone")
        case .email: return UIImage(named: "support_email")
        case .telegram: return UIImage(named: "support_telegram")
        case .whatsApp: return UIImage(named: "support_whatsapp")
        }
    }
    
    var link: String {
        switch self {
        case .phone: return Const.Link.phone
        case .email: return Const.Link.email
        case .telegram: return Const.Link.telegram
        case .whatsApp: return Const.Link.whatsApp
        }
    }
    
    var isEnabled: Bool {
        switch self {
        default: return true
        }
    }
    
    var isDisclosureNeeded: Bool { true }
}
