import Foundation

protocol RecoverSeedPhraseModule: AnyObject {
  func setup(phoneNumber: String, password: String)
}
protocol RecoverSeedPhraseModuleDelegate: AnyObject {
  func finishRecoveringSeedPhrase()
}
