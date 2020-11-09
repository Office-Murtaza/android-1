import Foundation

protocol ManageWalletsModule: class {}
protocol ManageWalletsModuleDelegate: class {
  func didChangeVisibility()
}
