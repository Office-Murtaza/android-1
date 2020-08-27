import Foundation

protocol SeedPhraseModule: class {
  func setup(for mode: SeedPhraseMode)
}
protocol SeedPhraseModuleDelegate: class {
  func didFinishCopyingSeedPhrase()
}
