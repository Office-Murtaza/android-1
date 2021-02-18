import LocalAuthentication

extension LAContext {
    
    enum BiometricType: String {
        case none
        case touchID
        case faceID
    }

    var supportedBioAuthType: BiometricType {
        var error: NSError?

        guard self.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return .none
        }

        if #available(iOS 11.0, *) {
            switch self.biometryType {
            case .none:
                return .none
            case .touchID:
                return .touchID
            case .faceID:
                return .faceID
            @unknown default:
                return .none
            }
        } else {
            return  self.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil) ? .touchID : .none
        }
    }
    
    func enroll(enrolled: @escaping (()->Void), failure: @escaping(()->Void)) {
        
        var error: NSError?
        
        guard self.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return
        }
        
        self.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: "Please authenticate to proceed.") { [enrolled, failure] (success, error) in
            if success {
                DispatchQueue.main.async { [enrolled] in
                    self.invalidate()
                    enrolled()
                }
            } else {
                guard let error = error else { return }
                print(error.localizedDescription)
                DispatchQueue.main.async { [failure] in
                    self.invalidate()
                    failure()
                }
            }
        }
        
    }
}
