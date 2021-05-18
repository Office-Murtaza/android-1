import Foundation

protocol ManageWalletsModule: AnyObject {}
protocol ManageWalletsModuleDelegate: AnyObject {
  func didChangeVisibility()
}
