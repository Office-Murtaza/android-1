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
    static let receiveGift = "coin_details.receive_gift"
    static let receiveSwap = "coin_details.receive_swap"
    static let reserve = "coin_details.reserve"
    static let reserved = "coin_details.reserved"
    static let se1f = "coin_details.se1f"
    static let sell = "coin_details.sell"
    static let sendGift = "coin_details.send_gift"
    static let sendSwap = "coin_details.send_swap"
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
    static let addGif = "coin_send_gift.add_gif"
    static let addNote = "coin_send_gift.add_note"
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

  struct Deals {
    static let title = "deals.title"
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

  struct Notifications {
    static let title = "notifications.title"
  }

  struct Phone {
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
    static let generate = "seed_phrase.generate"
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

  struct Support {
    static let title = "support.title"
  }

  struct Swap {
    static let percent = "swap.percent"
    static let platformfee = "swap.platformfee"
    static let tilda = "swap.tilda"
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

  struct Transfer {
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
    static let compliantPolicy = "about.cell.compliant_policy"
    static let privacyPolicy = "about.cell.privacy_policy"
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

  struct Error {
    static let transactionError = "coin_details.error.transaction_error"
  }

  struct Status {
    static let available = "coin_details.status.available"
    static let canceled = "coin_details.status.canceled"
    static let complete = "coin_details.status.complete"
    static let disputing = "coin_details.status.disputing"
    static let doing = "coin_details.status.doing"
    static let fail = "coin_details.status.fail"
    static let new = "coin_details.status.new"
    static let notAvailable = "coin_details.status.not_available"
    static let notExist = "coin_details.status.not_exist"
    static let paid = "coin_details.status.paid"
    static let pending = "coin_details.status.pending"
    static let released = "coin_details.status.released"
    static let unknown = "coin_details.status.unknown"
    static let withdrawn = "coin_details.status.withdrawn"
  }

  struct Success {
    static let transactionCreated = "coin_details.success.transaction_created"
  }

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

  struct ErrorStatus {
    static let cancel = "coin_staking.error_status.cancel"
    static let creation = "coin_staking.error_status.creation"
    static let withdraw = "coin_staking.error_status.withdraw"
  }

  struct Header {
    static let annualRewardAmount = "coin_staking.header.annual_reward_amount"
    static let cancelHoldPeriod = "coin_staking.header.cancel_hold_period"
    static let createdDate = "coin_staking.header.created_date"
    static let stakingAnualPercent = "coin_staking.header.staking_anual_percent"
  }

  struct Status {
    static let cancelPending = "coin_staking.status.cancel_pending"
    static let canceled = "coin_staking.status.canceled"
    static let createPending = "coin_staking.status.create_pending"
    static let created = "coin_staking.status.created"
    static let formViewStatus = "coin_staking.status.form_view_status"
    static let withdrawPending = "coin_staking.status.withdraw_pending"
  }

  struct SuccessStatus {
    static let canceled = "coin_staking.success_status.canceled"
    static let created = "coin_staking.success_status.created"
    static let withdrawn = "coin_staking.success_status.withdrawn"
  }

  struct Toast {
    static let completed = "coin_staking.toast.completed"
    static let error = "coin_staking.toast.error"
  }

  struct WithdrawView {
    static let description = "coin_staking.withdraw_view.description"
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
    static let value = "coin_staking.header.reward.value"
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

  struct UsdConverted {
    static let title = "coin_staking.header.usd_converted.title"
    static let value = "coin_staking.header.usd_converted.value"
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
extension L.Deals {

  struct Cell {
    static let staking = "deals.cell.staking"
    static let swap = "deals.cell.swap"
    static let transfer = "deals.cell.transfer"
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
extension L.Notifications {

  struct Cell {
    static let title = "notifications.cell.title"
  }
}
extension L.Phone {

  struct Default {
    static let prefix = "phone.default.prefix"
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
    static let faceId = "security.cell.face_id"
    static let seedPhrase = "security.cell.seed_phrase"
    static let touchId = "security.cell.touch_id"
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
    static let notifications = "settings.cell.notifications"
    static let security = "settings.cell.security"
    static let support = "settings.cell.support"
    static let wallet = "settings.cell.wallet"
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
extension L.Support {

  struct Cell {
    static let email = "support.cell.email"
    static let phone = "support.cell.phone"
    static let telegram = "support.cell.telegram"
    static let whatsApp = "support.cell.whats_app"
  }
}
extension L.Swap {

  struct Usd {
    static let title = "swap.usd.title"
  }
}
extension L.TransactionDetails {

  struct Header {
  }

  struct HeaderTitle {
    static let amount = "transaction_details.header_title.amount"
    static let cashStatus = "transaction_details.header_title.cash_status"
    static let confirmations = "transaction_details.header_title.confirmations"
    static let date = "transaction_details.header_title.date"
    static let fee = "transaction_details.header_title.fee"
    static let fromAddress = "transaction_details.header_title.from_address"
    static let fromPhone = "transaction_details.header_title.from_phone"
    static let id = "transaction_details.header_title.id"
    static let sellAmount = "transaction_details.header_title.sell_amount"
    static let status = "transaction_details.header_title.status"
    static let swapAmount = "transaction_details.header_title.swap_amount"
    static let swapId = "transaction_details.header_title.swap_id"
    static let toAddress = "transaction_details.header_title.to_address"
    static let toPhone = "transaction_details.header_title.to_phone"
    static let type = "transaction_details.header_title.type"
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
extension L.Transfer {

  struct Receiver {
    static let title = "transfer.receiver.title"
  }
}
extension L.Transfer.Receiver {

  struct Phone {
    static let placeholder = "transfer.receiver.phone.placeholder"
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

  struct Header {
    static let title = "wallet.header.title"
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
