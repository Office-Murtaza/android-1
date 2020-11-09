// Root structs 
enum L {

  struct About {
    static let title = "about.title"
  }

  struct Atm {
    static let title = "atm.title"
  }

  struct BuySellTradeDetails {
    static let title = "buy_sell_trade_details.title"
  }

  struct CoinDeposit {
    static let title = "coin_deposit.title"
  }

  struct CoinDetails {
    static let address = "coin_details.address"
    static let available = "coin_details.available"
    static let balance = "coin_details.balance"
    static let buy = "coin_details.buy"
    static let cancelStake = "coin_details.cancel_stake"
    static let complete = "coin_details.complete"
    static let createStake = "coin_details.create_stake"
    static let deposit = "coin_details.deposit"
    static let exchange = "coin_details.exchange"
    static let fail = "coin_details.fail"
    static let move = "coin_details.move"
    static let notAvailable = "coin_details.not_available"
    static let oneDay = "coin_details.one_day"
    static let oneMonth = "coin_details.one_month"
    static let oneWeek = "coin_details.one_week"
    static let oneYear = "coin_details.one_year"
    static let pending = "coin_details.pending"
    static let price = "coin_details.price"
    static let recall = "coin_details.recall"
    static let receiveC2C = "coin_details.receive_c_2_c"
    static let receiveGift = "coin_details.receive_gift"
    static let reserve = "coin_details.reserve"
    static let se1f = "coin_details.se1f"
    static let sell = "coin_details.sell"
    static let sendC2C = "coin_details.send_c_2_c"
    static let sendGift = "coin_details.send_gift"
    static let staking = "coin_details.staking"
    static let threeMonths = "coin_details.three_months"
    static let trade = "coin_details.trade"
    static let transactionCreated = "coin_details.transaction_created"
    static let unknown = "coin_details.unknown"
    static let withdraw = "coin_details.withdraw"
    static let withdrawStake = "coin_details.withdraw_stake"
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
    static let txLimit = "coin_sell.tx_limit"
  }

  struct CoinSellDetails {
    static let title = "coin_sell_details.title"
  }

  struct CoinSendGift {
    static let title = "coin_send_gift.title"
  }

  struct CoinStaking {
    static let title = "coin_staking.title"
  }

  struct CoinWithdraw {
    static let title = "coin_withdraw.title"
  }

  struct CreateEditTrade {
    static let createTitle = "create_edit_trade.create_title"
    static let editTitle = "create_edit_trade.edit_title"
    static let type = "create_edit_trade.type"
  }

  struct CreateWallet {
    static let title = "create_wallet.title"
  }

  struct EnterPassword {
  }

  struct Error {
  }

  struct KYC {
    static let title = "k_y_c.title"
  }

  struct ManageWallets {
    static let title = "manage_wallets.title"
  }

  struct PhoneVerification {
    static let codeSent = "phone_verification.code_sent"
    static let enterCode = "phone_verification.enter_code"
    static let resendCode = "phone_verification.resend_code"
    static let title = "phone_verification.title"
  }

  struct PinCode {
  }

  struct Recall {
    static let title = "recall.title"
  }

  struct Recover {
    static let title = "recover.title"
  }

  struct RecoverSeedPhrase {
    static let annotation = "recover_seed_phrase.annotation"
    static let title = "recover_seed_phrase.title"
  }

  struct Reserve {
    static let title = "reserve.title"
  }

  struct Security {
    static let title = "security.title"
  }

  struct SeedPhrase {
    static let annotation = "seed_phrase.annotation"
    static let title = "seed_phrase.title"
  }

  struct Settings {
    static let title = "settings.title"
  }

  struct Shared {
    static let cancel = "shared.cancel"
    static let copied = "shared.copied"
    static let ok = "shared.ok"
    static let termsAndConditions = "shared.terms_and_conditions"
  }

  struct ShowPhone {
    static let title = "show_phone.title"
  }

  struct Trades {
    static let buy = "trades.buy"
    static let create = "trades.create"
    static let recall = "trades.recall"
    static let reserve = "trades.reserve"
    static let reserved = "trades.reserved"
    static let sell = "trades.sell"
    static let title = "trades.title"
  }

  struct TransactionDetails {
    static let title = "transaction_details.title"
  }

  struct Unlink {
    static let annotation = "unlink.annotation"
    static let title = "unlink.title"
    static let unlinked = "unlink.unlinked"
  }

  struct UpdatePIN {
    static let pinUpdated = "update_p_i_n.pin_updated"
  }

  struct UpdatePassword {
    static let passwordUpdated = "update_password.password_updated"
    static let title = "update_password.title"
  }

  struct UpdatePhone {
    static let phoneUpdated = "update_phone.phone_updated"
    static let title = "update_phone.title"
  }

  struct VIPVerification {
    static let idSelfie = "v_i_p_verification.id_selfie"
    static let title = "v_i_p_verification.title"
  }

  struct Verification {
    static let idScan = "verification.id_scan"
    static let title = "verification.title"
  }

  struct Wallet {
    static let title = "wallet.title"
  }

  struct Welcome {
    static let contactSupport = "welcome.contact_support"
  }
}
// Extensions
extension L.About {

  struct Cell {
    static let support = "about.cell.support"
    static let termsAndConditions = "about.cell.terms_and_conditions"
    static let version = "about.cell.version"
  }
}
extension L.Atm {

  struct InfoWindow {
    static let openNow = "atm.info_window.open_now"
  }
}
extension L.BuySellTradeDetails {

  struct Button {
    static let sendRequest = "buy_sell_trade_details.button.send_request"
  }

  struct Form {
  }

  struct Header {
    static let limits = "buy_sell_trade_details.header.limits"
    static let payment = "buy_sell_trade_details.header.payment"
    static let price = "buy_sell_trade_details.header.price"
    static let terms = "buy_sell_trade_details.header.terms"
    static let user = "buy_sell_trade_details.header.user"
  }
}
extension L.BuySellTradeDetails.Form {

  struct Error {
    static let notWithinLimits = "buy_sell_trade_details.form.error.not_within_limits"
    static let tooManyCharacters = "buy_sell_trade_details.form.error.too_many_characters"
  }

  struct RequestDetails {
    static let placeholder = "buy_sell_trade_details.form.request_details.placeholder"
  }
}
extension L.CoinDetails {

  struct Transactions {
    static let empty = "coin_details.transactions.empty"
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
extension L.CoinStaking {

  struct Button {
    static let withdraw = "coin_staking.button.withdraw"
  }

  struct Header {
  }

  struct Status {
    static let cancelPending = "coin_staking.status.cancel_pending"
    static let canceled = "coin_staking.status.canceled"
    static let createPending = "coin_staking.status.create_pending"
    static let created = "coin_staking.status.created"
    static let withdrawPending = "coin_staking.status.withdraw_pending"
  }
}
extension L.CoinStaking.Header {

  struct Amount {
    static let title = "coin_staking.header.amount.title"
  }

  struct CancelDate {
    static let title = "coin_staking.header.cancel_date.title"
  }

  struct CancelPeriod {
    static let title = "coin_staking.header.cancel_period.title"
    static let value = "coin_staking.header.cancel_period.value"
  }

  struct CreateDate {
    static let title = "coin_staking.header.create_date.title"
  }

  struct Duration {
    static let title = "coin_staking.header.duration.title"
    static let value = "coin_staking.header.duration.value"
  }

  struct Reward {
    static let title = "coin_staking.header.reward.title"
  }

  struct RewardAnnual {
    static let title = "coin_staking.header.reward_annual.title"
  }

  struct Status {
    static let title = "coin_staking.header.status.title"
  }

  struct UntilWithdraw {
    static let title = "coin_staking.header.until_withdraw.title"
    static let value = "coin_staking.header.until_withdraw.value"
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

  struct CoinAmount {
    static let helper = "coin_withdraw.form.coin_amount.helper"
    static let placeholder = "coin_withdraw.form.coin_amount.placeholder"
  }

  struct CurrencyAmount {
    static let placeholder = "coin_withdraw.form.currency_amount.placeholder"
  }

  struct Error {
    static let insufficientETHBalance = "coin_withdraw.form.error.insufficient_e_t_h_balance"
    static let invalidAddress = "coin_withdraw.form.error.invalid_address"
    static let invalidAmount = "coin_withdraw.form.error.invalid_amount"
    static let lessThanFee = "coin_withdraw.form.error.less_than_fee"
    static let notEnoughToActivate = "coin_withdraw.form.error.not_enough_to_activate"
    static let tooHighAmount = "coin_withdraw.form.error.too_high_amount"
    static let tooLowAmount = "coin_withdraw.form.error.too_low_amount"
  }

  struct RecipientAddress {
    static let placeholder = "coin_withdraw.form.recipient_address.placeholder"
  }
}
extension L.CreateEditTrade {

  struct Form {
  }
}
extension L.CreateEditTrade.Form {

  struct Error {
    static let invalidMargin = "create_edit_trade.form.error.invalid_margin"
    static let invalidMaxLimit = "create_edit_trade.form.error.invalid_max_limit"
    static let invalidMinLimit = "create_edit_trade.form.error.invalid_min_limit"
    static let tooManyCharactersInPayment = "create_edit_trade.form.error.too_many_characters_in_payment"
    static let tooManyCharactersInTerms = "create_edit_trade.form.error.too_many_characters_in_terms"
  }

  struct Margin {
    static let placeholder = "create_edit_trade.form.margin.placeholder"
  }

  struct MaxLimit {
    static let placeholder = "create_edit_trade.form.max_limit.placeholder"
  }

  struct MinLimit {
    static let placeholder = "create_edit_trade.form.min_limit.placeholder"
  }

  struct Payment {
    static let placeholder = "create_edit_trade.form.payment.placeholder"
  }

  struct Terms {
    static let placeholder = "create_edit_trade.form.terms.placeholder"
  }
}
extension L.CreateWallet {

  struct Form {
  }
}
extension L.CreateWallet.Form {

  struct ConfirmPassword {
    static let placeholder = "create_wallet.form.confirm_password.placeholder"
  }

  struct Error {
    static let allFieldsRequired = "create_wallet.form.error.all_fields_required"
    static let existedPhoneNumber = "create_wallet.form.error.existed_phone_number"
    static let fieldRequired = "create_wallet.form.error.field_required"
    static let notEqualPasswords = "create_wallet.form.error.not_equal_passwords"
    static let notValidPassword = "create_wallet.form.error.not_valid_password"
    static let notValidPhoneNumber = "create_wallet.form.error.not_valid_phone_number"
  }

  struct Password {
    static let placeholder = "create_wallet.form.password.placeholder"
  }

  struct Phone {
    static let placeholder = "create_wallet.form.phone.placeholder"
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

  struct Password {
    static let placeholder = "enter_password.form.password.placeholder"
  }
}
extension L.Error {

  struct NoConnection {
    static let subtitle = "error.no_connection.subtitle"
    static let title = "error.no_connection.title"
  }

  struct ServerError {
    static let subtitle = "error.server_error.subtitle"
    static let title = "error.server_error.title"
  }

  struct SomethingWentWrong {
    static let subtitle = "error.something_went_wrong.subtitle"
    static let title = "error.something_went_wrong.title"
  }
}
extension L.KYC {

  struct Button {
    static let verify = "k_y_c.button.verify"
    static let vipVerify = "k_y_c.button.vip_verify"
  }

  struct Header {
  }
}
extension L.KYC.Header {

  struct DailyLimit {
    static let title = "k_y_c.header.daily_limit.title"
  }

  struct Status {
    static let title = "k_y_c.header.status.title"
  }

  struct TransactionLimit {
    static let title = "k_y_c.header.transaction_limit.title"
  }
}
extension L.KYC.Header.Status {

  struct Value {
    static let notVerified = "k_y_c.header.status.value.not_verified"
    static let unknown = "k_y_c.header.status.value.unknown"
    static let verificationPending = "k_y_c.header.status.value.verification_pending"
    static let verificationRejected = "k_y_c.header.status.value.verification_rejected"
    static let verified = "k_y_c.header.status.value.verified"
    static let vipVerificationPending = "k_y_c.header.status.value.vip_verification_pending"
    static let vipVerificationRejected = "k_y_c.header.status.value.vip_verification_rejected"
    static let vipVerified = "k_y_c.header.status.value.vip_verified"
  }
}
extension L.PhoneVerification {

  struct Error {
    static let invalidCode = "phone_verification.error.invalid_code"
  }
}
extension L.PinCode {

  struct PinType {
  }

  struct Stage {
  }
}
extension L.PinCode.PinType {

  struct Current {
    static let title = "pin_code.pin_type.current.title"
  }

  struct New {
    static let title = "pin_code.pin_type.new.title"
  }

  struct Old {
    static let title = "pin_code.pin_type.old.title"
  }
}
extension L.PinCode.Stage {

  struct Confirmation {
    static let title = "pin_code.stage.confirmation.title"
  }

  struct Setup {
    static let title = "pin_code.stage.setup.title"
  }

  struct Verification {
    static let title = "pin_code.stage.verification.title"
  }
}
extension L.Recall {

  struct Button {
    static let recall = "recall.button.recall"
  }

  struct Form {
  }
}
extension L.Recall.Form {

  struct Error {
    static let tooLowAmount = "recall.form.error.too_low_amount"
  }
}
extension L.Recover {

  struct Form {
  }
}
extension L.Recover.Form {

  struct Error {
    static let notExistedPhoneNumber = "recover.form.error.not_existed_phone_number"
    static let notMatchPassword = "recover.form.error.not_match_password"
  }
}
extension L.RecoverSeedPhrase {

  struct Form {
  }
}
extension L.RecoverSeedPhrase.Form {

  struct Error {
    static let notValidLength = "recover_seed_phrase.form.error.not_valid_length"
  }
}
extension L.Reserve {

  struct Button {
    static let reserve = "reserve.button.reserve"
  }
}
extension L.Security {

  struct Cell {
    static let seedPhrase = "security.cell.seed_phrase"
    static let unlink = "security.cell.unlink"
    static let updatePIN = "security.cell.update_p_i_n"
    static let updatePassword = "security.cell.update_password"
    static let updatePhone = "security.cell.update_phone"
  }
}
extension L.Settings {

  struct Cell {
    static let about = "settings.cell.about"
    static let kyc = "settings.cell.kyc"
    static let security = "settings.cell.security"
  }
}
extension L.Shared {

  struct Button {
    static let add = "shared.button.add"
    static let cancel = "shared.button.cancel"
    static let copy = "shared.button.copy"
    static let create = "shared.button.create"
    static let done = "shared.button.done"
    static let goBack = "shared.button.go_back"
    static let max = "shared.button.max"
    static let next = "shared.button.next"
    static let paste = "shared.button.paste"
    static let remove = "shared.button.remove"
    static let retry = "shared.button.retry"
    static let send = "shared.button.send"
    static let submit = "shared.button.submit"
    static let unlink = "shared.button.unlink"
    static let update = "shared.button.update"
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
extension L.TransactionDetails {

  struct Header {
  }
}
extension L.TransactionDetails.Header {

  struct Amount {
    static let title = "transaction_details.header.amount.title"
  }

  struct CashStatus {
    static let title = "transaction_details.header.cash_status.title"
  }

  struct Date {
    static let title = "transaction_details.header.date.title"
  }

  struct Fee {
    static let title = "transaction_details.header.fee.title"
  }

  struct FromAddress {
    static let title = "transaction_details.header.from_address.title"
  }

  struct FromUser {
    static let title = "transaction_details.header.from_user.title"
  }

  struct ID {
    static let title = "transaction_details.header.i_d.title"
  }

  struct Image {
    static let title = "transaction_details.header.image.title"
  }

  struct Message {
    static let title = "transaction_details.header.message.title"
  }

  struct RefAmount {
    static let title = "transaction_details.header.ref_amount.title"
  }

  struct RefCoin {
    static let title = "transaction_details.header.ref_coin.title"
  }

  struct RefID {
    static let title = "transaction_details.header.ref_i_d.title"
  }

  struct SellQRCode {
    static let title = "transaction_details.header.sell_q_r_code.title"
  }

  struct Status {
    static let title = "transaction_details.header.status.title"
  }

  struct ToAddress {
    static let title = "transaction_details.header.to_address.title"
  }

  struct ToUser {
    static let title = "transaction_details.header.to_user.title"
  }

  struct TxType {
    static let title = "transaction_details.header.tx_type.title"
  }
}
extension L.UpdatePassword {

  struct Form {
  }
}
extension L.UpdatePassword.Form {

  struct ConfirmNewPassword {
    static let placeholder = "update_password.form.confirm_new_password.placeholder"
  }

  struct Error {
    static let samePassword = "update_password.form.error.same_password"
  }

  struct NewPassword {
    static let placeholder = "update_password.form.new_password.placeholder"
  }

  struct OldPassword {
    static let placeholder = "update_password.form.old_password.placeholder"
  }
}
extension L.UpdatePhone {

  struct Form {
  }
}
extension L.UpdatePhone.Form {

  struct Error {
    static let phoneUsed = "update_phone.form.error.phone_used"
    static let samePhone = "update_phone.form.error.same_phone"
  }

  struct Phone {
    static let placeholder = "update_phone.form.phone.placeholder"
  }
}
extension L.VIPVerification {

  struct Form {
  }
}
extension L.VIPVerification.Form {

  struct Error {
    static let notValidSSN = "v_i_p_verification.form.error.not_valid_s_s_n"
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
    static let cityRequired = "verification.form.error.city_required"
    static let countryRequired = "verification.form.error.country_required"
    static let idScanRequired = "verification.form.error.id_scan_required"
    static let imageBroken = "verification.form.error.image_broken"
    static let notValidAddress = "verification.form.error.not_valid_address"
    static let notValidFirstName = "verification.form.error.not_valid_first_name"
    static let notValidIdNumber = "verification.form.error.not_valid_id_number"
    static let notValidLastName = "verification.form.error.not_valid_last_name"
    static let notValidZipCode = "verification.form.error.not_valid_zip_code"
    static let provinceRequired = "verification.form.error.province_required"
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
extension L.Wallet {

  struct Footer {
  }

  struct Header {
    static let title = "wallet.header.title"
  }
}
extension L.Wallet.Footer {

  struct Button {
    static let manageWallets = "wallet.footer.button.manage_wallets"
  }
}
extension L.Welcome {

  struct CreateButton {
    static let title = "welcome.create_button.title"
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
    static let call = "welcome.support.call"
    static let mail = "welcome.support.mail"
    static let message = "welcome.support.message"
    static let phone = "welcome.support.phone"
    static let send = "welcome.support.send"
  }

  struct ThirdSlide {
    static let title = "welcome.third_slide.title"
  }
}
