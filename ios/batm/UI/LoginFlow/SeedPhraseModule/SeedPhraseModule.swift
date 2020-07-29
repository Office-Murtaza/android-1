import Foundation

protocol SeedPhraseModule: class {
  func setup(phoneNumber: String, password: String)
}
protocol SeedPhraseModuleDelegate: class {
  func finishCopyingSeedPhrase()
}
