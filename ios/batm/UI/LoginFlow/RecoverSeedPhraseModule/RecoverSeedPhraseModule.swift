import Foundation

protocol RecoverSeedPhraseModule: class {}
protocol RecoverSeedPhraseModuleDelegate: class {
  func finishRecoveringSeedPhrase()
}
