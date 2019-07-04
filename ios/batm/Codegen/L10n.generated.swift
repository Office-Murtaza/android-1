// Root structs 
enum L {

  struct CreateWallet {
    static let title = "create_wallet.title"
  }

  struct SeedPhrase {
    static let annotation = "seed_phrase.annotation"
    static let title = "seed_phrase.title"
  }

  struct Shared {
    static let cancel = "shared.cancel"
    static let copy = "shared.copy"
    static let done = "shared.done"
    static let next = "shared.next"
    static let ok = "shared.ok"
  }

  struct Welcome {
    static let contactSupport = "welcome.contact_support"
    static let recoverWallet = "welcome.recover_wallet"
    static let tagline = "welcome.tagline"
    static let termsAndConditions = "welcome.terms_and_conditions"
  }
}
// Extensions
extension L.CreateWallet {

  struct Code {
    static let placeholder = "create_wallet.code.placeholder"
  }

  struct Form {
  }
}
extension L.CreateWallet.Code {

  struct Error {
    static let title = "create_wallet.code.error.title"
  }
}
extension L.CreateWallet.Form {

  struct ConfirmPassword {
    static let placeholder = "create_wallet.form.confirm_password.placeholder"
  }

  struct Error {
    static let allFieldsRequired = "create_wallet.form.error.all_fields_required"
    static let notEqualPasswords = "create_wallet.form.error.not_equal_passwords"
    static let notValidPhoneNumber = "create_wallet.form.error.not_valid_phone_number"
  }

  struct Password {
    static let placeholder = "create_wallet.form.password.placeholder"
  }

  struct PhoneNumber {
    static let placeholder = "create_wallet.form.phone_number.placeholder"
  }
}
extension L.Shared {

  struct Error {
    static let message = "shared.error.message"
    static let title = "shared.error.title"
  }
}
extension L.Welcome {

  struct CreateButton {
    static let title = "welcome.create_button.title"
  }

  struct Error {
    static let title = "welcome.error.title"
  }

  struct FirstSlide {
    static let title = "welcome.first_slide.title"
  }

  struct LoginButton {
    static let title = "welcome.login_button.title"
  }

  struct SecondSlide {
    static let title = "welcome.second_slide.title"
  }

  struct Support {
    static let mail = "welcome.support.mail"
    static let phone = "welcome.support.phone"
    static let title = "welcome.support.title"
  }

  struct ThirdSlide {
    static let title = "welcome.third_slide.title"
  }
}