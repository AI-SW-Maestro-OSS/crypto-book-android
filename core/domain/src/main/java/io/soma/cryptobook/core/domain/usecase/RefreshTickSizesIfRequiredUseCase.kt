package io.soma.cryptobook.core.domain.usecase

import io.soma.cryptobook.core.domain.repository.TickSizeRepository
import javax.inject.Inject

class RefreshTickSizesIfRequiredUseCase @Inject constructor(
    private val tickSizeRepository: TickSizeRepository,
) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
        tickSizeRepository.refreshTickSizesIfRequired(nowMillis)
    }
}
