package io.soma.cryptobook.core.designsystem.component.scaffold.model

import io.soma.cryptobook.core.designsystem.component.navigation.model.NavigationItem
import kotlinx.collections.immutable.ImmutableList

data class ScaffoldNavigationData(
    val onNavigationClick: (NavigationItem) -> Unit,
    val navigationItems: ImmutableList<NavigationItem>,
    val selectedNavigationItem: NavigationItem?,
)