package io.soma.cryptobook.core.network.market

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WsMarketMessageParserTest {
    private val parser = WsMarketMessageParser(
        json = Json {
            ignoreUnknownKeys = true
        },
    )

    @Test
    fun `parse returns all mini tickers for mini ticker array payload`() {
        val raw = """
            [
              {
                "e":"24hrMiniTicker",
                "s":"BTCUSDT",
                "c":"102.5",
                "o":"100.0",
                "h":"103.0",
                "l":"99.0",
                "v":"10.0",
                "q":"1025.0"
              },
              {
                "e":"24hrMiniTicker",
                "s":"ETHUSDT",
                "c":"205.0",
                "o":"200.0",
                "h":"206.0",
                "l":"198.0",
                "v":"20.0",
                "q":"4100.0"
              }
            ]
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertTrue(parsed is WsMarketMessage.AllMiniTickers)
        val message = parsed as WsMarketMessage.AllMiniTickers
        assertEquals(2, message.tickers.size)
        assertEquals("BTCUSDT", message.tickers.first().symbol)
        assertEquals("102.5", message.tickers.first().lastPrice)
    }
}
