import Foundation

protocol SeedPhraseModule: AnyObject {
  func setup(for mode: SeedPhraseMode)
}
protocol SeedPhraseModuleDelegate: AnyObject {
  func didFinishCopyingSeedPhrase()
}
