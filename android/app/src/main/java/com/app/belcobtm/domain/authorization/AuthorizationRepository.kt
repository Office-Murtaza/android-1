package com.app.belcobtm.domain.authorization

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface AuthorizationRepository {
    suspend fun clearAppData(): Either<Failure, Unit>
}