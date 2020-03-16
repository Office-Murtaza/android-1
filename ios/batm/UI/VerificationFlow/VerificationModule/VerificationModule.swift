import UIKit

protocol VerificationModule: class {
  func didPick(image: UIImage)
}
protocol VerificationModuleDelegate: class {
  func showPicker(from module: VerificationModule)
  func didFinishVerification(with info: VerificationInfo?)
}
