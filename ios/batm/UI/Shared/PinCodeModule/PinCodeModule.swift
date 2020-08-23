import Foundation

protocol PinCodeModule: class {
  func setup(for stage: PinCodeStage)
  func setup(for type: PinCodeType)
  func setup(with correctCode: String)
  func setup(shouldShowNavBar: Bool)
}
protocol PinCodeModuleDelegate: class {
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String)
}
