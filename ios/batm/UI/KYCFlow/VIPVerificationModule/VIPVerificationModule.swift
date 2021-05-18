import UIKit

protocol VIPVerificationModule: AnyObject {
  func didPick(image: UIImage)
}
protocol VIPVerificationModuleDelegate: AnyObject {
  func showPicker(from module: VIPVerificationModule)
  func didFinishVIPVerification()
}
