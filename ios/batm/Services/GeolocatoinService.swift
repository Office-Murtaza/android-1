import Foundation
import CoreLocation

class GeolocationService: NSObject, CLLocationManagerDelegate {
    
    private let manager: CLLocationManager
    typealias LocationResult = (CLLocation?) -> Void
    private var resultClosure: LocationResult?
    override init() {
        manager = CLLocationManager()
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    func requestLocation(result: @escaping LocationResult) {
        manager.requestWhenInUseAuthorization()
        manager.requestLocation()
        resultClosure = result
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        resultClosure?(locations.first)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("error")
    }
}
