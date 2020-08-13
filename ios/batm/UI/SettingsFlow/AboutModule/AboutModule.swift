import Foundation

protocol AboutModule: class {}
protocol AboutModuleDelegate: class {
  func didSelectTermsAndConditions()
  func didSelectSupport()
  func didSelectVersion()
}
