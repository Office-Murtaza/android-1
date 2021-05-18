import Foundation

protocol TransferModule: AnyObject {
    func resetState() 
}
protocol TransferModuleDelegate: AnyObject {
    func showSendGift(contact: BContact)
}
