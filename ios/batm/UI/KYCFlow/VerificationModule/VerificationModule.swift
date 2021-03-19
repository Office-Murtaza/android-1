import UIKit

protocol VerificationModule: AnyObject {
  func didPick(image: UIImage)
}
protocol VerificationModuleDelegate: AnyObject {
  func showPicker(from module: VerificationModule)
  func didFinishVerification()
}
