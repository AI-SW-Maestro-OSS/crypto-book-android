package io.soma.cryptobook.core.domain.usecase

import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(private val userDataRepository: UserDataRepository) {
    operator fun invoke(): Flow<UserData> = userDataRepository.userData
}
