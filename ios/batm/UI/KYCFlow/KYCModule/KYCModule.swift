import Foundation

protocol KYCModule: class {
  func setup(with kyc: KYC)
}
protocol KYCModuleDelegate: class {
  func didFinishKYC()
  func didSelectVerify(from module: KYCModule)
  func didSelectVIPVerify(from module: KYCModule)
}
