package io.soma.cryptobook.core.designsystem.theme.component.navigation.model

interface NavigationItem {
    val iconResSelected: Int
    val iconRes: Int
    val labelRes: Int
    val graphRoute: Any
    val startDestinationRoute: Any
}