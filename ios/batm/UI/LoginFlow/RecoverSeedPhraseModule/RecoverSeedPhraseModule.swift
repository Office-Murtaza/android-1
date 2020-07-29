import Foundation

protocol RecoverSeedPhraseModule: class {
  func setup(phoneNumber: String, password: String)
}
protocol RecoverSeedPhraseModuleDelegate: class {
  func cancelRecoveringSeedPhrase()
  func finishRecoveringSeedPhrase()
}
