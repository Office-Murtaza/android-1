import UIKit
import RxSwift
import RxCocoa
import SnapKit
import GoogleMaps

class ATMViewController: ModuleViewController<ATMPresenter>, GMSMapViewDelegate, CLLocationManagerDelegate {
  
  let mapView = GMSMapView()
  
  var currentLocation: CLLocation? {
    didSet {
      guard let location = currentLocation else { return }
      
      let camera = GMSCameraPosition.camera(withLatitude: location.coordinate.latitude,
                                            longitude: location.coordinate.longitude,
                                            zoom: 15.0)
      mapView.animate(to: camera)
    }
  }
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override func setupUI() {
    view.addSubview(mapView)
    
    mapView.delegate = self
    mapView.isMyLocationEnabled = true
  }
  
  override func setupLayout() {
    mapView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.mapAddresses == nil }
      .bind(to: view.rx.showHUD)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.mapAddresses }
      .filterNil()
      .map { $0.addresses }
      .drive(onNext: { [mapView] in
        $0.forEach {
          let marker = GMSMarker()
          marker.position = CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude)
          marker.infoWindowAnchor = CGPoint(x: 0.5, y: 0.5)
          marker.userData = $0
          marker.map = mapView
        }
      })
      .disposed(by: disposeBag)
    
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    presenter.bind(input: ATMPresenter.Input())
  }
  
  func mapView(_ mapView: GMSMapView, markerInfoWindow marker: GMSMarker) -> UIView? {
    guard let mapAddress = marker.userData as? MapAddress else { return nil }
    
    let infoView = ATMInfoView()
    infoView.configure(for: mapAddress)
    infoView.layoutIfNeeded()
    return infoView
  }
  
  func mapViewDidFinishTileRendering(_ mapView: GMSMapView) {
    if currentLocation == nil {
      currentLocation = mapView.myLocation
    }
  }
  
}
