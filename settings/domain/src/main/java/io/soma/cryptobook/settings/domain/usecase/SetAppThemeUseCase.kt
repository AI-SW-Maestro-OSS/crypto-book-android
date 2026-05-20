package io.soma.cryptobook.settings.domain.usecase

import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetAppThemeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(appTheme: AppTheme) {
        userDataRepository.setAppTheme(appTheme)
    }
}
