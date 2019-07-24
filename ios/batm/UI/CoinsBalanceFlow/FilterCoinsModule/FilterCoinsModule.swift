import Foundation

protocol FilterCoinsModule: class {}
protocol FilterCoinsModuleDelegate: class {
  func didFinishFiltering()
  func didChangeVisibility()
}
