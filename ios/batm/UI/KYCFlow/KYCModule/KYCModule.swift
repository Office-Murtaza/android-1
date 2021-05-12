import Foundation

protocol KYCModule: AnyObject {}

protocol KYCModuleDelegate: AnyObject {
    func didFinishKYC()
    func didSelectVerify(from module: KYCModule)
    func didSelectVIPVerify(from module: KYCModule)
}
