import Foundation

protocol UnlinkModule: class {
  func unlink()
}
protocol UnlinkModuleDelegate: class {
  func didFinishUnlink()
  func didUnlink(from module: UnlinkModule)
}
