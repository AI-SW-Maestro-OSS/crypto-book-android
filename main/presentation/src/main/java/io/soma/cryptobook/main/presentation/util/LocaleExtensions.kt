package io.soma.cryptobook.main.presentation.util

import io.soma.cryptobook.core.domain.model.Language
import java.util.Locale

/**
 * 시스템에서 받은 [Locale]을 우리 도메인의 [Language] enum 으로 매핑한다.
 * 지원하지 않은 locale 이면 null 반환.
 */
internal fun Locale.toLanguage(): Language? = Language.entries
    .find { it.localeTag.equals(language, ignoreCase = true) }
