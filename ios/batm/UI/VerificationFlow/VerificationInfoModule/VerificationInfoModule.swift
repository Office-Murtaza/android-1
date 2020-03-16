import Foundation

protocol VerificationInfoModule: class {
  func setup(with info: VerificationInfo)
}
protocol VerificationInfoModuleDelegate: class {
  func didFinishVerificationInfo()
  func didSelectVerify(from module: VerificationInfoModule)
  func didSelectVIPVerify(from module: VerificationInfoModule)
}
