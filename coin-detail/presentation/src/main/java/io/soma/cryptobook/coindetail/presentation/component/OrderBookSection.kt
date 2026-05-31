package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

internal const val ORDER_BOOK_ROW_COUNT = 32
private const val DEPTH_BAR_ALPHA = 0.12f
private const val EMPTY_VALUE = "--"

private enum class OrderBookSide { Bid, Ask }

/**
 * Order book section showing aggregated bid/ask depth.
 *
 * Figma element name: OrderBookSection
 * Figma element type: Frame
 * Figma node-id: 327:660
 *
 * Displays (top to bottom):
 * - Header: "Order Book" title and a tick-size selector
 * - Pressure bar: bid/ask percentages and a proportional bar
 * - Column header: "Bid" / "Ask" labels
 * - List: exactly [ORDER_BOOK_ROW_COUNT] rows, each with a bid and an ask cell
 *
 * Dependencies: None (all sub-components are private helpers in this file)
 *
 * Layout:
 * - Column, fillMaxWidth, background = background.primary
 * - The list renders all rows at natural height (no internal scroll)
 *
 * Each cell draws a cumulative depth bar behind the text, anchored at the
 * center of the row and growing outward as [OrderBookEntry.depthRatio] increases.
 * A null bid/ask renders an empty ("--") cell with no depth bar.
 *
 * @param tickSize Formatted tick size shown in the selector (e.g., "0.01")
 * @param bidPercentText Formatted bid pressure (e.g., "41.93%")
 * @param askPercentText Formatted ask pressure (e.g., "58.07%")
 * @param bidRatio Bid share of the pressure bar in 0f..1f (ask share = 1 - bidRatio)
 * @param rows Exactly [ORDER_BOOK_ROW_COUNT] rows; throws otherwise
 * @param onTickSizeClick Invoked when the tick-size selector is clicked
 * @param modifier Optional modifier
 */
@Composable
fun OrderBookSection(
    tickSize: String,
    bidPercentText: String,
    askPercentText: String,
    bidRatio: Float,
    rows: List<OrderBookRowUiModel>,
    onTickSizeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(rows.size == ORDER_BOOK_ROW_COUNT) {
        "OrderBookSection requires exactly $ORDER_BOOK_ROW_COUNT rows but was ${rows.size}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CbTheme.colorScheme.background.primary),
    ) {
        OrderBookHeader(tickSize = tickSize, onTickSizeClick = onTickSizeClick)
        OrderBookPressureBar(
            bidPercentText = bidPercentText,
            askPercentText = askPercentText,
            bidRatio = bidRatio,
        )
        OrderBookColumnHeader()
        OrderBookList(rows = rows)
    }
}

data class OrderBookRowUiModel(val bid: OrderBookEntry?, val ask: OrderBookEntry?)

data class OrderBookEntry(val price: String, val quantity: String, val depthRatio: Float)

@Composable
private fun OrderBookHeader(
    tickSize: String,
    onTickSizeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(CryptoString.cb_coin_detail_order_book),
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            color = CbTheme.colorScheme.text.primary,
        )
        TickSizeSelector(tickSize = tickSize, onClick = onTickSizeClick)
    }
}

@Composable
private fun TickSizeSelector(
    tickSize: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CbTheme.colorScheme.background.tertiary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = tickSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = CbTheme.colorScheme.text.primary,
        )
        Icon(
            painter = painterResource(CbDrawable.ic_sort_desc),
            contentDescription = null,
            tint = CbTheme.colorScheme.text.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun OrderBookPressureBar(
    bidPercentText: String,
    askPercentText: String,
    bidRatio: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = bidPercentText,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = CbTheme.colorScheme.price.up,
            )
            Text(
                text = askPercentText,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = CbTheme.colorScheme.price.down,
            )
        }
        val safeBidRatio = bidRatio.coerceIn(0f, 1f)
        val safeAskRatio = 1f - safeBidRatio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(6.dp)
                .clip(CircleShape),
        ) {
            if (safeBidRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(safeBidRatio)
                        .fillMaxHeight()
                        .background(CbTheme.colorScheme.price.up),
                )
            }
            if (safeAskRatio > 0f) {
                Box(
                    modifier = Modifier
                        .weight(safeAskRatio)
                        .fillMaxHeight()
                        .background(CbTheme.colorScheme.price.down),
                )
            }
        }
    }
}

@Composable
private fun OrderBookColumnHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CbTheme.colorScheme.background.tertiary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(CryptoString.cb_coin_detail_bid),
            modifier = Modifier.weight(1f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = CbTheme.colorScheme.text.secondary,
        )
        Text(
            text = stringResource(CryptoString.cb_coin_detail_ask),
            modifier = Modifier.weight(1f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = CbTheme.colorScheme.text.secondary,
        )
    }
}

@Composable
private fun OrderBookList(rows: List<OrderBookRowUiModel>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CbTheme.colorScheme.background.primary),
    ) {
        rows.forEach { row ->
            OrderBookRow(row = row)
        }
    }
}

@Composable
private fun OrderBookRow(row: OrderBookRowUiModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrderBookCell(
            entry = row.bid,
            side = OrderBookSide.Bid,
            modifier = Modifier.weight(1f),
        )
        OrderBookCell(
            entry = row.ask,
            side = OrderBookSide.Ask,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OrderBookCell(
    entry: OrderBookEntry?,
    side: OrderBookSide,
    modifier: Modifier = Modifier,
) {
    val priceColor = when (side) {
        OrderBookSide.Bid -> CbTheme.colorScheme.price.up
        OrderBookSide.Ask -> CbTheme.colorScheme.price.down
    }
    val quantityColor = CbTheme.colorScheme.text.secondary

    Box(modifier = modifier.height(24.dp)) {
        if (entry != null) {
            val barAlignment = when (side) {
                OrderBookSide.Bid -> Alignment.CenterEnd
                OrderBookSide.Ask -> Alignment.CenterStart
            }
            Box(
                modifier = Modifier
                    .align(barAlignment)
                    .fillMaxWidth(entry.depthRatio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(priceColor.copy(alpha = DEPTH_BAR_ALPHA)),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (side) {
                OrderBookSide.Bid -> {
                    OrderBookCellText(text = entry?.quantity ?: EMPTY_VALUE, color = quantityColor)
                    OrderBookCellText(text = entry?.price ?: EMPTY_VALUE, color = priceColor)
                }

                OrderBookSide.Ask -> {
                    OrderBookCellText(text = entry?.price ?: EMPTY_VALUE, color = priceColor)
                    OrderBookCellText(text = entry?.quantity ?: EMPTY_VALUE, color = quantityColor)
                }
            }
        }
    }
}

@Composable
private fun OrderBookCellText(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = color,
    )
}

private fun orderBookPreviewRows(): List<OrderBookRowUiModel> {
    val bidLevels = 20
    val askLevels = 18
    return List(ORDER_BOOK_ROW_COUNT) { index ->
        OrderBookRowUiModel(
            bid = if (index < bidLevels) {
                OrderBookEntry(
                    price = "73,${519 + index}.05",
                    quantity = "0.00${114 + index}",
                    depthRatio = (index + 1) / bidLevels.toFloat(),
                )
            } else {
                null
            },
            ask = if (index < askLevels) {
                OrderBookEntry(
                    price = "73,${521 + index}.08",
                    quantity = "0.1${728 + index}",
                    depthRatio = (index + 1) / askLevels.toFloat(),
                )
            } else {
                null
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun OrderBookSectionPreview() {
    OrderBookSection(
        tickSize = "0.01",
        bidPercentText = "41.93%",
        askPercentText = "58.07%",
        bidRatio = 0.4193f,
        rows = orderBookPreviewRows(),
        onTickSizeClick = {},
    )
}
