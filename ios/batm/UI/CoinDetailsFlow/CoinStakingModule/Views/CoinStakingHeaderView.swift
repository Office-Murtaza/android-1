import UIKit
import RxSwift
import RxCocoa

class CoinStakingHeaderView: UIView {
  
  static var defaultVerticalStackView: UIStackView {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.alignment = .leading
    stackView.spacing = 15
    return stackView
  }
  
  static func defaultTitleLabel(_ title: String) -> UILabel {
    let label = UILabel()
    label.text = title
    label.textColor = .slateGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }
  
  static var defaultValueLabel: UILabel {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }
  
  let mainStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 20
    return stackView
  }()
  
  let titleStackView = defaultVerticalStackView
  let valueStackView = defaultVerticalStackView
  
  let priceTitleView = UIView()
  let priceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.price))
  let priceValueLabel = defaultValueLabel
  
  let balanceTitleView = UIView()
  let balanceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.balance))
  let balanceValueView = CoinDetailsBalanceValueView()
  
  let stakedTitleView = UIView()
  let stakedTitleLabel = defaultTitleLabel(localize(L.CoinStaking.Header.Staked.title))
  let stakedValueLabel = defaultValueLabel
  
  let rewardsTitleView = UIView()
  let rewardsTitleLabel = defaultTitleLabel(localize(L.CoinStaking.Header.Rewards.title))
  let rewardsValueLabel = defaultValueLabel
  
  let durationTitleView = UIView()
  let durationTitleLabel = defaultTitleLabel(localize(L.CoinStaking.Header.Duration.title))
  let durationValueLabel = defaultValueLabel
  
  let minDurationTitleView = UIView()
  let minDurationTitleLabel = defaultTitleLabel(localize(L.CoinStaking.Header.MinDuration.title))
  let minDurationValueLabel = defaultValueLabel
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(mainStackView)
    
    mainStackView.addArrangedSubviews(titleStackView,
                                      valueStackView)
    
    priceTitleView.addSubview(priceTitleLabel)
    balanceTitleView.addSubview(balanceTitleLabel)
    stakedTitleView.addSubview(stakedTitleLabel)
    rewardsTitleView.addSubview(rewardsTitleLabel)
    durationTitleView.addSubview(durationTitleLabel)
    minDurationTitleView.addSubview(minDurationTitleLabel)
    
    titleStackView.addArrangedSubviews(priceTitleView,
                                       balanceTitleView,
                                       stakedTitleView,
                                       rewardsTitleView,
                                       durationTitleView,
                                       minDurationTitleView)
    
    valueStackView.addArrangedSubviews(priceValueLabel,
                                       balanceValueView,
                                       stakedValueLabel,
                                       rewardsValueLabel,
                                       durationValueLabel,
                                       minDurationValueLabel)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    [priceTitleLabel,
     balanceTitleLabel,
     stakedTitleLabel,
     rewardsTitleLabel,
     durationTitleLabel,
     minDurationTitleLabel].forEach {
      $0.snp.makeConstraints {
        $0.top.left.right.equalToSuperview()
      }
    }
    
    titleStackView.arrangedSubviews.enumerated().forEach { index, subview in
      subview.snp.makeConstraints {
        $0.height.equalTo(valueStackView.arrangedSubviews[index])
      }
    }
  }
  
  func configure(for coinBalance: CoinBalance, stakeDetails: StakeDetails) {
    priceValueLabel.text = coinBalance.price.fiatFormatted.withUSD
    balanceValueView.configure(for: coinBalance)
    stakedValueLabel.text = "\(stakeDetails.stakedAmount ?? 0) \(coinBalance.type.code)"
    rewardsValueLabel.text = "\(stakeDetails.rewardsAmount ?? 0) \(coinBalance.type.code), \(stakeDetails.rewardsPercent ?? 0) %"
    durationValueLabel.text = String(format: localize(L.CoinStaking.Header.Duration.value), stakeDetails.stakedDays ?? 0)
    minDurationValueLabel.text = String(format: localize(L.CoinStaking.Header.MinDuration.value), stakeDetails.stakingMinDays ?? 0)
    
    [stakedTitleView,
     stakedValueLabel,
     rewardsTitleView,
     rewardsValueLabel,
     durationTitleView,
     durationValueLabel,
     minDurationTitleView,
     minDurationValueLabel].forEach {
      $0.isHidden = !stakeDetails.exist
    }
  }
}
