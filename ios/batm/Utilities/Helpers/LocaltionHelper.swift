import CoreLocation

final class UserLocationManager: NSObject {
    private let locationManager = CLLocationManager()
    
    override init() {
        super.init()
        setupLocationManager()
    }
    
    func getUserLongitude() -> Double {
        guard let longitude = locationManager.location?.coordinate.longitude else { return 0 }
        return longitude
    }
    
    func getUserLatitude() -> Double {
        guard let latitude = locationManager.location?.coordinate.latitude else { return 0 }
        return latitude
    }
    
    private func setupLocationManager() {
        locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()
        }
    }
}

extension UserLocationManager: CLLocationManagerDelegate {}
