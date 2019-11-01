// Root structs 
enum L {

  struct Atm {
    static let title = "atm.title"
  }

  struct ChangePassword {
    static let title = "change_password.title"
  }

  struct ChangePhone {
    static let title = "change_phone.title"
  }

  struct ChangePin {
    static let title = "change_pin.title"
  }

  struct CoinDetails {
    static let address = "coin_details.address"
    static let balance = "coin_details.balance"
    static let buy = "coin_details.buy"
    static let complete = "coin_details.complete"
    static let deposit = "coin_details.deposit"
    static let fail = "coin_details.fail"
    static let pending = "coin_details.pending"
    static let price = "coin_details.price"
    static let receiveGift = "coin_details.receive_gift"
    static let sell = "coin_details.sell"
    static let sendGift = "coin_details.send_gift"
    static let unknown = "coin_details.unknown"
    static let withdraw = "coin_details.withdraw"
  }

  struct CoinSell {
    static let dailyLimit = "coin_sell.daily_limit"
    static let sellFromAnother = "coin_sell.sell_from_another"
    static let title = "coin_sell.title"
    static let transactionLimit = "coin_sell.transaction_limit"
  }

  struct CoinSellDetails {
    static let title = "coin_sell_details.title"
  }

  struct CoinSendGift {
    static let title = "coin_send_gift.title"
  }

  struct CoinWithdraw {
    static let title = "coin_withdraw.title"
  }

  struct CoinsBalance {
    static let title = "coins_balance.title"
    static let totalBalance = "coins_balance.total_balance"
  }

  struct CreateWallet {
    static let title = "create_wallet.title"
  }

  struct EnterPassword {
    static let title = "enter_password.title"
  }

  struct FilterCoins {
    static let hide = "filter_coins.hide"
    static let show = "filter_coins.show"
    static let title = "filter_coins.title"
  }

  struct PinCode {
  }

  struct Recover {
    static let title = "recover.title"
  }

  struct RecoverSeedPhrase {
    static let annotation = "recover_seed_phrase.annotation"
  }

  struct SeedPhrase {
    static let annotation = "seed_phrase.annotation"
    static let title = "seed_phrase.title"
  }

  struct Settings {
    static let changePassword = "settings.change_password"
    static let changePin = "settings.change_pin"
    static let phone = "settings.phone"
    static let showSeedPhrase = "settings.show_seed_phrase"
    static let title = "settings.title"
    static let unlink = "settings.unlink"
  }

  struct Shared {
    static let addGif = "shared.add_gif"
    static let cancel = "shared.cancel"
    static let change = "shared.change"
    static let copy = "shared.copy"
    static let done = "shared.done"
    static let max = "shared.max"
    static let next = "shared.next"
    static let ok = "shared.ok"
    static let paste = "shared.paste"
    static let removeGif = "shared.remove_gif"
    static let scan = "shared.scan"
  }

  struct ShowPhone {
    static let title = "show_phone.title"
  }

  struct ShowSeedPhrase {
    static let title = "show_seed_phrase.title"
  }

  struct TransactionDetails {
    static let amount = "transaction_details.amount"
    static let date = "transaction_details.date"
    static let fee = "transaction_details.fee"
    static let fromAddress = "transaction_details.from_address"
    static let image = "transaction_details.image"
    static let message = "transaction_details.message"
    static let phone = "transaction_details.phone"
    static let status = "transaction_details.status"
    static let title = "transaction_details.title"
    static let toAddress = "transaction_details.to_address"
    static let txId = "transaction_details.tx_id"
    static let type = "transaction_details.type"
  }

  struct Unlink {
    static let title = "unlink.title"
    static let warning = "unlink.warning"
  }

  struct Welcome {
    static let contactSupport = "welcome.contact_support"
    static let tagline = "welcome.tagline"
    static let termsAndConditions = "welcome.terms_and_conditions"
  }
}
// Extensions
extension L.Atm {

  struct InfoWindow {
    static let openNow = "atm.info_window.open_now"
  }
}
extension L.ChangePassword {

  struct Form {
  }
}
extension L.ChangePassword.Form {

  struct ConfirmNewPassword {
    static let placeholder = "change_password.form.confirm_new_password.placeholder"
  }

  struct NewPassword {
    static let placeholder = "change_password.form.new_password.placeholder"
  }

  struct OldPassword {
    static let placeholder = "change_password.form.old_password.placeholder"
  }
}
extension L.ChangePin {

  struct Form {
  }
}
extension L.ChangePin.Form {

  struct ConfirmNewPin {
    static let placeholder = "change_pin.form.confirm_new_pin.placeholder"
  }

  struct Error {
    static let notEqualPins = "change_pin.form.error.not_equal_pins"
    static let notMatch = "change_pin.form.error.not_match"
    static let wrongLength = "change_pin.form.error.wrong_length"
  }

  struct NewPin {
    static let placeholder = "change_pin.form.new_pin.placeholder"
  }

  struct OldPin {
    static let placeholder = "change_pin.form.old_pin.placeholder"
  }
}
extension L.CoinSell {

  struct Form {
  }
}
extension L.CoinSell.Form {

  struct Error {
    static let notMultiple = "coin_sell.form.error.not_multiple"
  }
}
extension L.CoinSellDetails {

  struct AnotherAddress {
    static let firstInstruction = "coin_sell_details.another_address.first_instruction"
    static let fourthInstruction = "coin_sell_details.another_address.fourth_instruction"
    static let secondInstruction = "coin_sell_details.another_address.second_instruction"
    static let thirdInstruction = "coin_sell_details.another_address.third_instruction"
  }

  struct CurrentAddress {
    static let firstInstruction = "coin_sell_details.current_address.first_instruction"
    static let fourthInstruction = "coin_sell_details.current_address.fourth_instruction"
    static let secondInstruction = "coin_sell_details.current_address.second_instruction"
    static let thirdInstruction = "coin_sell_details.current_address.third_instruction"
  }
}
extension L.CoinSendGift {

  struct Form {
  }
}
extension L.CoinSendGift.Form {

  struct Error {
    static let invalidPhone = "coin_send_gift.form.error.invalid_phone"
  }

  struct Message {
    static let placeholder = "coin_send_gift.form.message.placeholder"
  }

  struct Phone {
    static let title = "coin_send_gift.form.phone.title"
  }
}
extension L.CoinWithdraw {

  struct Form {
  }
}
extension L.CoinWithdraw.Form {

  struct Address {
    static let title = "coin_withdraw.form.address.title"
  }

  struct Amount {
    static let title = "coin_withdraw.form.amount.title"
  }

  struct Error {
    static let invalidAddress = "coin_withdraw.form.error.invalid_address"
    static let invalidAmount = "coin_withdraw.form.error.invalid_amount"
    static let tooHighAmount = "coin_withdraw.form.error.too_high_amount"
    static let tooLowAmount = "coin_withdraw.form.error.too_low_amount"
  }
}
extension L.CoinsBalance {

  struct Footer {
    static let title = "coins_balance.footer.title"
  }
}
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
}
extension L.EnterPassword {

  struct Form {
  }
}
extension L.EnterPassword.Form {

  struct Error {
    static let wrongPassword = "enter_password.form.error.wrong_password"
  }
}
extension L.PinCode {

  struct Setup {
    static let title = "pin_code.setup.title"
  }

  struct Verification {
    static let title = "pin_code.verification.title"
  }
}
extension L.Shared {

  struct Error {
    static let message = "shared.error.message"
    static let title = "shared.error.title"
  }
}
extension L.Unlink {

  struct Button {
    static let title = "unlink.button.title"
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

  struct RecoverButton {
    static let title = "welcome.recover_button.title"
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
