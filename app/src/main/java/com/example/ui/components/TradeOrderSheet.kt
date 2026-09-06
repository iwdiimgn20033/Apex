package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.AssetCategory
import com.example.data.model.OrderSide
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import java.util.Locale

@Composable
fun TradeOrderSheet(
    asset: Asset,
    language: AppLanguage,
    freeMargin: Double,
    initialSide: OrderSide = OrderSide.BUY,
    initialStopLoss: Double? = null,
    initialTakeProfit: Double? = null,
    onDismiss: () -> Unit,
    onExecuteOrder: (side: OrderSide, lots: Double, leverage: Int, sl: Double?, tp: Double?) -> Unit
) {
    var selectedSide by remember { mutableStateOf(initialSide) }
    var lotSize by remember { mutableDoubleStateOf(if (asset.category == AssetCategory.FOREX) 1.0 else 10.0) }
    var leverage by remember { mutableIntStateOf(if (asset.category == AssetCategory.FOREX) 100 else 20) }
    var stopLossText by remember { mutableStateOf(initialStopLoss?.let { String.format(Locale.US, "%.4f", it) } ?: "") }
    var takeProfitText by remember { mutableStateOf(initialTakeProfit?.let { String.format(Locale.US, "%.4f", it) } ?: "") }
    var orderPlacedSuccess by remember { mutableStateOf(false) }

    val execPrice = if (selectedSide == OrderSide.BUY) asset.ask else asset.bid
    val lotMultiplier = if (asset.category == AssetCategory.FOREX) 100000.0 else 1.0
    val notionalValue = execPrice * lotSize * lotMultiplier
    val requiredMargin = notionalValue / leverage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("trade_order_sheet"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${asset.symbol} - ${asset.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "MT5 Live: ${asset.mt5Symbol}  |  Spread: ${asset.spreadPips} pips",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_order_sheet")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (orderPlacedSuccess) {
                // Success feedback
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = BullishGreen,
                        modifier = Modifier.height(56.dp).width(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تم إرسال الأمر لخادم MT5 بنجاح!" else "Order Executed on MT5 Live Server!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BullishGreen
                    )
                    Text(
                        text = "${selectedSide.name} $lotSize Lots @ ${String.format(Locale.US, "%.4f", execPrice)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Buy / Sell Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    // Buy Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSide == OrderSide.BUY) BullishGreen else Color.Transparent)
                            .clickable { selectedSide = OrderSide.BUY }
                            .padding(vertical = 12.dp)
                            .testTag("select_buy_side"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = Strings.get("buy", language),
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSide == OrderSide.BUY) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.4f", asset.ask),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedSide == OrderSide.BUY) Color.Black else BullishGreen
                            )
                        }
                    }

                    // Sell Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSide == OrderSide.SELL) BearishRed else Color.Transparent)
                            .clickable { selectedSide = OrderSide.SELL }
                            .padding(vertical = 12.dp)
                            .testTag("select_sell_side"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = Strings.get("sell", language),
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSide == OrderSide.SELL) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.4f", asset.bid),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedSide == OrderSide.SELL) Color.White else BearishRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lot Size Stepper
                Text(
                    text = Strings.get("lots", language),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val step = if (asset.category == AssetCategory.FOREX) 0.1 else 1.0
                            lotSize = (lotSize - step).coerceAtLeast(0.01)
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .testTag("lot_size_minus")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f Lots", lotSize),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            val step = if (asset.category == AssetCategory.FOREX) 0.1 else 1.0
                            lotSize += step
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .testTag("lot_size_plus")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Leverage Selector
                Text(
                    text = Strings.get("leverage", language),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 10, 20, 50, 100).forEach { lev ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (leverage == lev) ApexCyan else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { leverage = lev }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1:$lev",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (leverage == lev) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SL & TP Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = stopLossText,
                        onValueChange = { stopLossText = it },
                        label = { Text("SL Price", fontSize = 12.sp) },
                        placeholder = { Text(String.format(Locale.US, "%.2f", execPrice * 0.98), fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_stop_loss"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BearishRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = takeProfitText,
                        onValueChange = { takeProfitText = it },
                        label = { Text("TP Price", fontSize = 12.sp) },
                        placeholder = { Text(String.format(Locale.US, "%.2f", execPrice * 1.05), fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_take_profit"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BullishGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Order Margin & Risk Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Required Margin:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", requiredMargin)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (requiredMargin > freeMargin) BearishRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Available Margin:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", freeMargin)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Execute Button
                Button(
                    onClick = {
                        val sl = stopLossText.toDoubleOrNull()
                        val tp = takeProfitText.toDoubleOrNull()
                        onExecuteOrder(selectedSide, lotSize, leverage, sl, tp)
                        orderPlacedSuccess = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("execute_order_button"),
                    enabled = requiredMargin <= freeMargin,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSide == OrderSide.BUY) BullishGreen else BearishRed,
                        contentColor = if (selectedSide == OrderSide.BUY) Color.Black else Color.White
                    )
                ) {
                    Text(
                        text = "${Strings.get("place_order", language)} (${selectedSide.name} $lotSize Lots)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
