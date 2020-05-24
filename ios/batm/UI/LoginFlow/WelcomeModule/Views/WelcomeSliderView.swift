import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class WelcomeSliderView: UIView, UIScrollViewDelegate, HasDisposeBag {
  
  let scrollView: UIScrollView = {
    let scrollView = UIScrollView()
    scrollView.isPagingEnabled = true
    scrollView.showsHorizontalScrollIndicator = false
    scrollView.bounces = false
    return scrollView
  }()
  
  let firstSlideView: WelcomeSlideView = {
    let view = WelcomeSlideView()
    view.configure(for: .first)
    return view
  }()
  
  let secondSlideView: WelcomeSlideView = {
    let view = WelcomeSlideView()
    view.configure(for: .second)
    return view
  }()
  
  let thirdSlideView: WelcomeSlideView = {
    let view = WelcomeSlideView()
    view.configure(for: .third)
    return view
  }()
  
  let pageControl: MDCPageControl = {
    let pageControl = MDCPageControl()
    pageControl.numberOfPages = 3
    pageControl.pageIndicatorTintColor = UIColor.brownishGrey.withAlphaComponent(0.4)
    pageControl.currentPageIndicatorTintColor = .ceruleanBlue
    return pageControl
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(scrollView,
                pageControl)
    
    scrollView.addSubviews(firstSlideView,
                           secondSlideView,
                           thirdSlideView)
    
    scrollView.delegate = self
  }
  
  private func setupLayout() {
    scrollView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    firstSlideView.snp.makeConstraints {
      $0.top.left.bottom.size.equalToSuperview()
    }
    secondSlideView.snp.makeConstraints {
      $0.left.equalTo(firstSlideView.snp.right)
      $0.top.bottom.size.equalToSuperview()
    }
    thirdSlideView.snp.makeConstraints {
      $0.left.equalTo(secondSlideView.snp.right)
      $0.top.right.bottom.size.equalToSuperview()
    }
    pageControl.snp.makeConstraints {
      $0.top.equalTo(scrollView.snp.bottom).offset(25)
      $0.bottom.centerX.equalToSuperview()
    }
  }
  
  func setupBindings() {
    pageControl.rx.controlEvent(.valueChanged)
      .subscribe(onNext: { [unowned self] in self.didChangePage() })
      .disposed(by: disposeBag)
  }
  
  private func didChangePage() {
    var offset = scrollView.contentOffset
    offset.x = CGFloat(pageControl.currentPage) * scrollView.bounds.size.width;
    scrollView.setContentOffset(offset, animated: true)
  }
  
  func scrollViewDidScroll(_ scrollView: UIScrollView) {
    pageControl.scrollViewDidScroll(scrollView)
  }

  func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
    pageControl.scrollViewDidEndDecelerating(scrollView)
  }

  func scrollViewDidEndScrollingAnimation(_ scrollView: UIScrollView) {
    pageControl.scrollViewDidEndScrollingAnimation(scrollView)
  }
}
