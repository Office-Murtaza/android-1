import UIKit

protocol VIPVerificationModule: class {
  func didPick(image: UIImage)
}
protocol VIPVerificationModuleDelegate: class {
  func showPicker(from module: VIPVerificationModule)
  func didFinishVIPVerification(with info: VerificationInfo?)
}
