import Foundation

protocol TransferModule: class {
    func resetState() 
}
protocol TransferModuleDelegate: class {
    func showSendGift(contact: BContact)
}
