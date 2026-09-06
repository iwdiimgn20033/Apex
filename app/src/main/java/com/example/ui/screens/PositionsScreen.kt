package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderSide
import com.example.data.model.Position
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.viewmodel.BrokerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PositionsScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val openPositions by viewModel.openPositions.collectAsStateWithLifecycle()
    val closedPositions by viewModel.closedPositions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Open Positions, 1 = Closed Trades

    val totalFloatingPnl = openPositions.sumOf { it.floatingPnL }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("positions_screen")
    ) {
        // Summary Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("positions_summary_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.get("total_floating_pnl", language),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (totalFloatingPnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", totalFloatingPnl)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalFloatingPnl >= 0) BullishGreen else BearishRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${Strings.get("used_margin", language)}: $${String.format(Locale.US, "%,.2f", userProfile.usedMargin)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Strings.get("free_margin", language)}: $${String.format(Locale.US, "%,.2f", userProfile.freeMargin)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Open / Closed Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp)
                    .testTag("tab_open_positions"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${Strings.get("open_positions", language)} (${openPositions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp)
                    .testTag("tab_closed_positions"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${Strings.get("closed_trades", language)} (${closedPositions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            if (openPositions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(Strings.get("no_open_positions", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(openPositions) { pos ->
                        OpenPositionCard(
                            position = pos,
                            language = language,
                            onClose = { viewModel.closePosition(pos.id) }
                        )
                    }
                }
            }
        } else {
            if (closedPositions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(Strings.get("no_closed_trades", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(closedPositions) { pos ->
                        ClosedPositionCard(position = pos, language = language)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenPositionCard(
    position: Position,
    language: AppLanguage,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("position_card_${position.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (position.side == OrderSide.BUY) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = position.side.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (position.side == OrderSide.BUY) BullishGreen else BearishRed
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${position.symbol} • ${position.volumeLots} ${Strings.get("lots", language)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${if (position.isProfitable) "+" else ""}$${String.format(Locale.US, "%,.2f", position.floatingPnL)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (position.isProfitable) BullishGreen else BearishRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Strings.get("entry_price", language)}: ${String.format(Locale.US, "%.4f", position.openPrice)} -> ${Strings.get("current_price", language)}: ${String.format(Locale.US, "%.4f", position.currentPrice)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${Strings.get("leverage", language)} 1:${position.leverage}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ApexCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Strings.get("position_id", language)}: #${position.mt5Ticket} • ${Strings.get("required_margin", language)}: $${String.format(Locale.US, "%.2f", position.marginRequired)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier.height(32.dp).testTag("close_pos_${position.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BearishRed.copy(alpha = 0.15f),
                        contentColor = BearishRed
                    )
                ) {
                    Text(Strings.get("close_position", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClosedPositionCard(position: Position, language: AppLanguage) {
    val pnl = position.realizedPnL ?: 0.0
    val formattedDate = position.closeTimestamp?.let {
        SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(it))
    } ?: "Closed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${position.symbol} (${position.side.name} ${position.volumeLots} ${Strings.get("lots", language)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${Strings.get("close_price", language)}: ${String.format(Locale.US, "%.4f", position.closePrice ?: position.currentPrice)} • $formattedDate",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (pnl >= 0) "+" else ""}$${String.format(Locale.US, "%,.2f", pnl)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (pnl >= 0) BullishGreen else BearishRed
                )
                Text(
                    text = "MT5 #${position.mt5Ticket}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
