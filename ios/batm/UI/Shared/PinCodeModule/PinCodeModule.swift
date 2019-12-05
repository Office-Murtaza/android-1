import Foundation

protocol PinCodeModule: class {
  func setup(for stage: PinCodeStage)
}
protocol PinCodeModuleDelegate: class {
  func didFinishPinCode(for stage: PinCodeStage)
}
