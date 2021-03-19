import Foundation

protocol PinCodeModule: AnyObject {
  func setup(for stage: PinCodeStage)
  func setup(for type: PinCodeType)
  func setup(with correctCode: String)
  func setup(shouldShowNavBar: Bool)
  func setup(shouldUseLocalAuthOnStart isEnabled: Bool)
  func startLocalAuth()
}
protocol PinCodeModuleDelegate: AnyObject {
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String)
}
