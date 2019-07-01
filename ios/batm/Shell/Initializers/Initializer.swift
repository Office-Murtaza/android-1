import UIKit

typealias LaunchOptions = [UIApplication.LaunchOptionsKey: Any]

protocol Initializer {
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container)
}
