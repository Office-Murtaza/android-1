//
//  Int+Extensions.swift
//  batm
//
//  Created by Dmitriy Kolesnyk on 08.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import Foundation

extension Int {
    func timestampToStringDate(format: String) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.timeZone = TimeZone.current
        dateFormatter.dateFormat = format
        dateFormatter.amSymbol = "AM"
        dateFormatter.pmSymbol = "PM"
        let date = Date(timeIntervalSince1970: TimeInterval(self) / 1000)
        return dateFormatter.string(from: date)
    }
}
