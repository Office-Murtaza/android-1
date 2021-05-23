import UIKit
 
protocol P2PTextFieldDelegate: AnyObject {
    func textFieldDidDelete(_ textField: UITextField)
}

class P2PTextField: UITextField {
    weak var deleteDelegate: P2PTextFieldDelegate?
    
    override func deleteBackward() {
         super.deleteBackward()
        deleteDelegate?.textFieldDidDelete(self)
     }
}


