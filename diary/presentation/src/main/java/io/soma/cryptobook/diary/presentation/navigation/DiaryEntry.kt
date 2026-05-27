package io.soma.cryptobook.diary.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.diary.presentation.DiaryRoute

fun EntryProviderScope<NavKey>.diaryEntry() {
    entry<DiaryNavKey> {
        DiaryRoute()
    }
}
