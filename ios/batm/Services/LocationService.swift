import Foundation
import RxSwift
import RxCocoa
import CoreLocation

protocol LocationService {
  func requestLocationIfNeeded()
}

protocol LocationManager: AnyObject {
  var delegate: CLLocationManagerDelegate? { get set }
  
  func requestLocation()
  func requestWhenInUseAuthorization()
}

extension CLLocationManager: LocationManager {}

class LocationServiceImpl: NSObject, LocationService, CLLocationManagerDelegate, HasDisposeBag {
  
  let api: APIGateway
  let accountStorage: AccountStorage
  let locationUpdateDateStorage: LocationUpdateDateStorage
  let locationManager: LocationManager
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       locationUpdateDateStorage: LocationUpdateDateStorage,
       locationManager: LocationManager = CLLocationManager()) {
    self.api = api
    self.accountStorage = accountStorage
    self.locationUpdateDateStorage = locationUpdateDateStorage
    self.locationManager = locationManager
    super.init()
    
    self.locationManager.delegate = self
  }
  
  func requestLocationIfNeeded() {
    switch CLLocationManager.authorizationStatus() {
    case .authorizedAlways, .authorizedWhenInUse:
      requestLocationIfUpdatedMoreThanDayAgo()
    case .notDetermined:
      locationManager.requestWhenInUseAuthorization()
    default: break
    }
  }
  
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print("UPDATE_LOCATION_ERROR:", error)
  }
  
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    if let location = locations.first {
      accountStorage.get()
        .flatMapCompletable { [api] in api.updateLocation(userId: $0.userId,
                                                          latitude: location.coordinate.latitude,
                                                          longitude: location.coordinate.longitude) }
        .andThen(locationUpdateDateStorage.save(updateDate: Date()))
        .subscribe()
        .disposed(by: disposeBag)
    }
  }
  
  func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
    switch status {
    case .authorizedAlways, .authorizedWhenInUse:
      requestLocationIfUpdatedMoreThanDayAgo()
    default: break
    }
  }
  
  private func requestLocationIfUpdatedMoreThanDayAgo() {
    locationUpdateDateStorage.get()
      .subscribe(onSuccess: { [locationManager] updateDate in
        print(Date())
        print(Date().addingTimeInterval(-1 * 60 * 60 * 24))
        if Date().addingTimeInterval(-1 * 60 * 60 * 24).compare(updateDate) == .orderedDescending {
          locationManager.requestLocation()
        }
        }, onError: { [locationManager] error in
          if let storageError = error as? StorageError, storageError == .notFound {
            locationManager.requestLocation()
          }
        })
      .disposed(by: disposeBag)
  }
}
