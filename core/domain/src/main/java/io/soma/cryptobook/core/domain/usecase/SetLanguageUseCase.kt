package io.soma.cryptobook.core.domain.usecase

import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(language: Language) {
        userDataRepository.setLanguage(language)
    }
}
