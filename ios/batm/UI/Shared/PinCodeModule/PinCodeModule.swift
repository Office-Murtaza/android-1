import Foundation

protocol PinCodeModule: class {
  func setup(for stage: PinCodeStage)
  func setup(with correctCode: String)
}
protocol PinCodeModuleDelegate: class {
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String)
}
