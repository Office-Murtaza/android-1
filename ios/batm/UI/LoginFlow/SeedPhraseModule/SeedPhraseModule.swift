import Foundation

protocol SeedPhraseModule: class {}
protocol SeedPhraseModuleDelegate: class {
  func finishCopyingSeedPhrase()
}
