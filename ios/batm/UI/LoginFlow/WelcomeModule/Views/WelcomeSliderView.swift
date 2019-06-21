import UIKit
import RxSwift
import RxCocoa

class WelcomeSliderView: UIView, UIScrollViewDelegate {
  
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
  
  let pageControl: UIPageControl = {
    let pageControl = UIPageControl()
    pageControl.numberOfPages = 3
    pageControl.pageIndicatorTintColor = .pinkishGrey
    return pageControl
  }()
  
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
      $0.top.equalTo(scrollView.snp.bottom)
      $0.bottom.centerX.equalToSuperview()
    }
  }
  
  private func updateCurrentPage() {
    pageControl.currentPage = Int(scrollView.contentOffset.x / scrollView.frame.width)
  }
  
  func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
    if !decelerate {
      updateCurrentPage()
    }
  }
  
  func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
    updateCurrentPage()
  }
}
