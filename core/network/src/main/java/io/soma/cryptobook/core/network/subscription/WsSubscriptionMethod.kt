package io.soma.cryptobook.core.network.subscription

enum class WsSubscriptionMethod(val wireValue: String) {
    Subscribe("SUBSCRIBE"),
    Unsubscribe("UNSUBSCRIBE"),
    ListSubscriptions("LIST_SUBSCRIPTIONS"),
}
