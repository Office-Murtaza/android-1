//
//  TransferHideKeyboardView.swift
//  batm
//
//  Created by Dmytro Frolov on 14.01.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

class TransferHideKeyboardView: UIView {

    typealias HideKeyboardCallback = ()->Void
    
    var callback: HideKeyboardCallback?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = true
        let recogizer = UITapGestureRecognizer(target: self, action: #selector(hideKeybard))
        self.addGestureRecognizer(recogizer)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func hideKeybard() {
        callback?()
    }

}
