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

  struct CoinDeposit {
    static let title = "coin_deposit.title"
  }

  struct CoinDetails {
    static let address = "coin_details.address"
    static let available = "coin_details.available"
    static let balance = "coin_details.balance"
    static let buy = "coin_details.buy"
    static let c2cExchange = "coin_details.c2c_exchange"
    static let complete = "coin_details.complete"
    static let deposit = "coin_details.deposit"
    static let fail = "coin_details.fail"
    static let notAvailable = "coin_details.not_available"
    static let oneDay = "coin_details.one_day"
    static let oneMonth = "coin_details.one_month"
    static let oneWeek = "coin_details.one_week"
    static let oneYear = "coin_details.one_year"
    static let pending = "coin_details.pending"
    static let price = "coin_details.price"
    static let receiveC2C = "coin_details.receive_c_2_c"
    static let receiveGift = "coin_details.receive_gift"
    static let sell = "coin_details.sell"
    static let sendC2C = "coin_details.send_c_2_c"
    static let sendGift = "coin_details.send_gift"
    static let threeMonths = "coin_details.three_months"
    static let unknown = "coin_details.unknown"
    static let withdraw = "coin_details.withdraw"
    static let withdrawn = "coin_details.withdrawn"
  }

  struct CoinExchange {
    static let title = "coin_exchange.title"
  }

  struct CoinSell {
    static let annotation = "coin_sell.annotation"
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
    static let changePhone = "settings.change_phone"
    static let changePin = "settings.change_pin"
    static let showSeedPhrase = "settings.show_seed_phrase"
    static let title = "settings.title"
    static let unlinkWallet = "settings.unlink_wallet"
    static let verification = "settings.verification"
  }

  struct Shared {
    static let addGif = "shared.add_gif"
    static let cancel = "shared.cancel"
    static let change = "shared.change"
    static let copied = "shared.copied"
    static let copy = "shared.copy"
    static let done = "shared.done"
    static let max = "shared.max"
    static let next = "shared.next"
    static let ok = "shared.ok"
    static let paste = "shared.paste"
    static let remove = "shared.remove"
    static let removeGif = "shared.remove_gif"
    static let scan = "shared.scan"
    static let select = "shared.select"
    static let send = "shared.send"
  }

  struct ShowPhone {
    static let title = "show_phone.title"
  }

  struct ShowSeedPhrase {
    static let title = "show_seed_phrase.title"
  }

  struct TransactionDetails {
    static let cashStatus = "transaction_details.cash_status"
    static let cryptoAmount = "transaction_details.crypto_amount"
    static let date = "transaction_details.date"
    static let fee = "transaction_details.fee"
    static let fiatAmount = "transaction_details.fiat_amount"
    static let fromAddress = "transaction_details.from_address"
    static let image = "transaction_details.image"
    static let message = "transaction_details.message"
    static let phone = "transaction_details.phone"
    static let refAmount = "transaction_details.ref_amount"
    static let refCoin = "transaction_details.ref_coin"
    static let refTxId = "transaction_details.ref_tx_id"
    static let status = "transaction_details.status"
    static let title = "transaction_details.title"
    static let toAddress = "transaction_details.to_address"
    static let txDbId = "transaction_details.tx_db_id"
    static let txId = "transaction_details.tx_id"
    static let type = "transaction_details.type"
  }

  struct Unlink {
    static let annotation = "unlink.annotation"
    static let title = "unlink.title"
    static let warning = "unlink.warning"
  }

  struct VIPVerification {
    static let idSelfie = "v_i_p_verification.id_selfie"
    static let title = "v_i_p_verification.title"
  }

  struct Verification {
    static let idScan = "verification.id_scan"
    static let title = "verification.title"
  }

  struct VerificationInfo {
    static let title = "verification_info.title"
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
extension L.CoinExchange {

  struct Form {
  }
}
extension L.CoinExchange.Form {

  struct Amount {
    static let placeholder = "coin_exchange.form.amount.placeholder"
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

  struct Code {
    static let placeholder = "coin_send_gift.form.code.placeholder"
  }

  struct Error {
    static let invalidPhone = "coin_send_gift.form.error.invalid_phone"
  }

  struct Message {
    static let placeholder = "coin_send_gift.form.message.placeholder"
  }

  struct Phone {
    static let placeholder = "coin_send_gift.form.phone.placeholder"
  }
}
extension L.CoinWithdraw {

  struct Button {
    static let max = "coin_withdraw.button.max"
    static let next = "coin_withdraw.button.next"
    static let paste = "coin_withdraw.button.paste"
  }

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

  struct CoinAmount {
    static let placeholder = "coin_withdraw.form.coin_amount.placeholder"
  }

  struct CurrencyAmount {
    static let placeholder = "coin_withdraw.form.currency_amount.placeholder"
  }

  struct Error {
    static let invalidAddress = "coin_withdraw.form.error.invalid_address"
    static let invalidAmount = "coin_withdraw.form.error.invalid_amount"
    static let tooHighAmount = "coin_withdraw.form.error.too_high_amount"
    static let tooLowAmount = "coin_withdraw.form.error.too_low_amount"
  }

  struct RecipientAddress {
    static let placeholder = "coin_withdraw.form.recipient_address.placeholder"
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

  struct Confirmation {
    static let title = "pin_code.confirmation.title"
  }

  struct Setup {
    static let title = "pin_code.setup.title"
  }

  struct Verification {
    static let title = "pin_code.verification.title"
  }
}
extension L.Shared {

  struct Button {
    static let add = "shared.button.add"
    static let copy = "shared.button.copy"
    static let max = "shared.button.max"
    static let paste = "shared.button.paste"
    static let remove = "shared.button.remove"
  }

  struct Error {
    static let message = "shared.error.message"
    static let title = "shared.error.title"
  }
}
extension L.Shared.Error {

  struct NoConnection {
    static let message = "shared.error.no_connection.message"
  }
}
extension L.Unlink {

  struct Button {
    static let title = "unlink.button.title"
  }
}
extension L.VIPVerification {

  struct Form {
  }
}
extension L.VIPVerification.Form {

  struct Error {
    static let idSelfieRequired = "v_i_p_verification.form.error.id_selfie_required"
    static let ssnWrongLength = "v_i_p_verification.form.error.ssn_wrong_length"
  }

  struct SSN {
    static let placeholder = "v_i_p_verification.form.s_s_n.placeholder"
  }
}
extension L.Verification {

  struct Form {
  }

  struct Picker {
    static let title = "verification.picker.title"
  }
}
extension L.Verification.Form {

  struct Address {
    static let placeholder = "verification.form.address.placeholder"
  }

  struct City {
    static let placeholder = "verification.form.city.placeholder"
  }

  struct Country {
    static let placeholder = "verification.form.country.placeholder"
  }

  struct Error {
    static let idScanRequired = "verification.form.error.id_scan_required"
    static let imageBroken = "verification.form.error.image_broken"
    static let zipCodeWrongLength = "verification.form.error.zip_code_wrong_length"
  }

  struct FirstName {
    static let placeholder = "verification.form.first_name.placeholder"
  }

  struct IDNumber {
    static let placeholder = "verification.form.i_d_number.placeholder"
  }

  struct LastName {
    static let placeholder = "verification.form.last_name.placeholder"
  }

  struct Province {
    static let placeholder = "verification.form.province.placeholder"
  }

  struct ZipCode {
    static let placeholder = "verification.form.zip_code.placeholder"
  }
}
extension L.Verification.Picker {

  struct CameraOption {
    static let title = "verification.picker.camera_option.title"
  }

  struct LibraryOption {
    static let title = "verification.picker.library_option.title"
  }
}
extension L.VerificationInfo {

  struct Button {
    static let verify = "verification_info.button.verify"
    static let vipVerify = "verification_info.button.vip_verify"
  }

  struct DailyLimitRow {
    static let title = "verification_info.daily_limit_row.title"
  }

  struct StatusRow {
    static let title = "verification_info.status_row.title"
  }

  struct TxLimitRow {
    static let title = "verification_info.tx_limit_row.title"
  }
}
extension L.VerificationInfo.StatusRow {

  struct Value {
    static let notVerified = "verification_info.status_row.value.not_verified"
    static let unknown = "verification_info.status_row.value.unknown"
    static let verificationPending = "verification_info.status_row.value.verification_pending"
    static let verificationRejected = "verification_info.status_row.value.verification_rejected"
    static let verified = "verification_info.status_row.value.verified"
    static let vipVerificationPending = "verification_info.status_row.value.vip_verification_pending"
    static let vipVerificationRejected = "verification_info.status_row.value.vip_verification_rejected"
    static let vipVerified = "verification_info.status_row.value.vip_verified"
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
