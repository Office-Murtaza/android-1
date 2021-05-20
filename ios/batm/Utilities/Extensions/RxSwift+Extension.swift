//
//  RxSwift+Extension.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 20.05.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import RxSwift

extension PrimitiveSequence {
    func retry(maxAttempts: Int, delay: TimeInterval) -> PrimitiveSequence<Trait, Element> {
        return self.retryWhen { errors in
            return errors.enumerated().flatMap { (index, error) -> Observable<Int64> in
                if index <= maxAttempts {
                    return Observable<Int64>.timer(RxTimeInterval(delay), scheduler: MainScheduler.instance)
                } else {
                    return Observable.error(error)
                }
            }
        }
    }
}
