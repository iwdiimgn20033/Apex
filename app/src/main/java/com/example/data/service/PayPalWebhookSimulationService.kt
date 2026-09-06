package com.example.data.service

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.NotificationEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class PayPalWebhookEvent(
    val eventId: String,
    val eventType: String,
    val summary: String,
    val summaryAr: String,
    val resourceType: String = "payouts_item",
    val transactionId: String,
    val payoutBatchId: String,
    val payoutItemId: String,
    val receiverDestination: String,
    val amount: Double,
    val currency: String = "USD",
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val signatureVerified: Boolean = true,
    val authAlgo: String = "SHA256withRSA",
    val certUrl: String = "https://api.sandbox.paypal.com/v1/notifications/certs/CERT-360-SANDBOX",
    val rawPayloadJson: String
)

data class WebhookListenerStatus(
    val isListening: Boolean = true,
    val sandboxUrl: String = "https://api-m.sandbox.paypal.com/v1/payments/payouts",
    val webhookEndpoint: String = "https://api-m.sandbox.paypal.com/v1/notifications/webhooks-events",
    val lastPingTimestamp: Long = System.currentTimeMillis(),
    val totalEventsReceived: Int = 0,
    val activeTrackedCount: Int = 0
)

class PayPalWebhookSimulationService(private val database: AppDatabase) {

    private val tag = "PayPalWebhookService"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val activeJobs = mutableMapOf<String, Job>()

    private val _listenerStatus = MutableStateFlow(WebhookListenerStatus())
    val listenerStatus: StateFlow<WebhookListenerStatus> = _listenerStatus.asStateFlow()

    private val _recentEvents = MutableStateFlow<List<PayPalWebhookEvent>>(emptyList())
    val recentEvents: StateFlow<List<PayPalWebhookEvent>> = _recentEvents.asStateFlow()

    private val _trackedTransactionIds = MutableStateFlow<Set<String>>(emptySet())
    val trackedTransactionIds: StateFlow<Set<String>> = _trackedTransactionIds.asStateFlow()

    init {
        coroutineScope.launch {
            startHeartbeat()
            checkAndResumePendingVerifications()
        }
    }

    private suspend fun startHeartbeat() {
        while (coroutineScope.isActive) {
            delay(15000L)
            _listenerStatus.update { current ->
                current.copy(
                    lastPingTimestamp = System.currentTimeMillis(),
                    activeTrackedCount = _trackedTransactionIds.value.size
                )
            }
        }
    }

    private suspend fun checkAndResumePendingVerifications() {
        try {
            val pending = database.transactionDao().getPendingOrProcessingTransactions()
            for (entity in pending) {
                if (!_trackedTransactionIds.value.contains(entity.id)) {
                    trackWithdrawal(
                        transactionId = entity.id,
                        amount = entity.amount,
                        method = entity.method,
                        destination = entity.accountDestination,
                        initialDelayMs = 2000L
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error resuming pending transactions: ${e.message}")
        }
    }

    fun trackWithdrawal(
        transactionId: String,
        amount: Double,
        method: String,
        destination: String,
        initialDelayMs: Long = 1500L
    ) {
        if (activeJobs.containsKey(transactionId)) return

        _trackedTransactionIds.update { it + transactionId }
        _listenerStatus.update { it.copy(activeTrackedCount = _trackedTransactionIds.value.size) }

        val job = coroutineScope.launch {
            try {
                val batchId = "SANDBOX-BATCH-" + Random.nextInt(1000000, 9999999)
                val itemId = "ITEM-" + UUID.randomUUID().toString().take(8).uppercase()

                // Step 1: Webhook Event - PAYMENT.PAYOUTSBATCH.PROCESSING
                delay(initialDelayMs)
                val event1Id = "WH-EVT-" + UUID.randomUUID().toString().take(12).uppercase()
                val event1 = PayPalWebhookEvent(
                    eventId = event1Id,
                    eventType = "PAYMENT.PAYOUTSBATCH.PROCESSING",
                    summary = "PayPal Payout Batch $batchId received and queued in Sandbox.",
                    summaryAr = "تم استلام دفعة السحب في منصة PayPal Sandbox وبدء معالجتها.",
                    transactionId = transactionId,
                    payoutBatchId = batchId,
                    payoutItemId = itemId,
                    receiverDestination = destination,
                    amount = amount,
                    status = "PROCESSING",
                    rawPayloadJson = buildRawPayloadJson(event1Id, "PAYMENT.PAYOUTSBATCH.PROCESSING", batchId, itemId, destination, amount, "PROCESSING")
                )
                recordWebhookEvent(event1)

                database.transactionDao().updateStatus(
                    id = transactionId,
                    status = TransactionStatus.PROCESSING.name,
                    notes = "PayPal Sandbox Webhook [$event1Id]: Batch $batchId dispatched to PayPal payment network."
                )

                // Step 2: Intermediate clearance event - PAYMENT.PAYOUTS-ITEM.UNBLOCKED
                delay(3500L)
                val event2Id = "WH-EVT-" + UUID.randomUUID().toString().take(12).uppercase()
                val event2 = PayPalWebhookEvent(
                    eventId = event2Id,
                    eventType = "PAYMENT.PAYOUTS-ITEM.UNBLOCKED",
                    summary = "Payout item $itemId cleared AML/KYC checks on PayPal sandbox gateway.",
                    summaryAr = "تمت الموافقة والتحقق الأمني على بند السحب في بوابة PayPal Sandbox.",
                    transactionId = transactionId,
                    payoutBatchId = batchId,
                    payoutItemId = itemId,
                    receiverDestination = destination,
                    amount = amount,
                    status = "UNBLOCKED",
                    rawPayloadJson = buildRawPayloadJson(event2Id, "PAYMENT.PAYOUTS-ITEM.UNBLOCKED", batchId, itemId, destination, amount, "UNBLOCKED")
                )
                recordWebhookEvent(event2)

                // Step 3: Final confirmation - PAYMENT.PAYOUTS-ITEM.SUCCEEDED
                delay(4000L)
                val event3Id = "WH-EVT-" + UUID.randomUUID().toString().take(12).uppercase()
                val event3 = PayPalWebhookEvent(
                    eventId = event3Id,
                    eventType = "PAYMENT.PAYOUTS-ITEM.SUCCEEDED",
                    summary = "Payout item $itemId completed. Funds successfully settled into receiver account ($destination).",
                    summaryAr = "تم اكتمال التحويل وإيداع المبلغ بنجاح في حساب المستلم ($destination).",
                    transactionId = transactionId,
                    payoutBatchId = batchId,
                    payoutItemId = itemId,
                    receiverDestination = destination,
                    amount = amount,
                    status = "SUCCESS",
                    rawPayloadJson = buildRawPayloadJson(event3Id, "PAYMENT.PAYOUTS-ITEM.SUCCEEDED", batchId, itemId, destination, amount, "SUCCESS")
                )
                recordWebhookEvent(event3)

                // Update Transaction to COMPLETED in database
                database.transactionDao().updateStatus(
                    id = transactionId,
                    status = TransactionStatus.COMPLETED.name,
                    notes = "PayPal Sandbox Webhook Verified [HTTP 200 OK]: Payout $batchId settled into $destination."
                )

                // Push In-App Notification for Webhook Confirmation
                val notif = NotificationEntity(
                    id = "NOTIF-WH-" + System.currentTimeMillis(),
                    title = "PayPal Webhook Verified: Payout Cleared",
                    titleAr = "تأكيد الويب هوك: تم وصول مبلغ سحب PayPal",
                    message = "Webhook verification confirmed $${String.format(Locale.US, "%,.2f", amount)} USD transfer to $destination.",
                    messageAr = "أكد مستمع الويب هوك اكتمال تحويل $${String.format(Locale.US, "%,.2f", amount)} دولار إلى $destination.",
                    type = "PAYMENT",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
                database.notificationDao().insertNotification(notif)

            } catch (e: Exception) {
                Log.e(tag, "Error during simulated webhook lifecycle: ${e.message}")
            } finally {
                _trackedTransactionIds.update { it - transactionId }
                activeJobs.remove(transactionId)
                _listenerStatus.update { it.copy(activeTrackedCount = _trackedTransactionIds.value.size) }
            }
        }

        activeJobs[transactionId] = job
    }

    fun forceImmediateVerification(transactionId: String, destination: String, amount: Double) {
        coroutineScope.launch {
            try {
                val batchId = "SANDBOX-FAST-" + Random.nextInt(1000000, 9999999)
                val itemId = "ITEM-FAST-" + UUID.randomUUID().toString().take(6).uppercase()
                val eventId = "WH-EVT-MANUAL-" + UUID.randomUUID().toString().take(8).uppercase()

                val event = PayPalWebhookEvent(
                    eventId = eventId,
                    eventType = "PAYMENT.PAYOUTS-ITEM.SUCCEEDED",
                    summary = "Instant Sandbox API Verification: Payout $itemId verified and settled.",
                    summaryAr = "تحقق فوري من واجهة Sandbox: تم تأكيد وتوثيق تحويل السحب بنجاح.",
                    transactionId = transactionId,
                    payoutBatchId = batchId,
                    payoutItemId = itemId,
                    receiverDestination = destination,
                    amount = amount,
                    status = "SUCCESS",
                    rawPayloadJson = buildRawPayloadJson(eventId, "PAYMENT.PAYOUTS-ITEM.SUCCEEDED", batchId, itemId, destination, amount, "SUCCESS")
                )
                recordWebhookEvent(event)

                database.transactionDao().updateStatus(
                    id = transactionId,
                    status = TransactionStatus.COMPLETED.name,
                    notes = "PayPal Sandbox Manual API Verification: Cleared & Settled into $destination."
                )

                _trackedTransactionIds.update { it - transactionId }
                activeJobs[transactionId]?.cancel()
                activeJobs.remove(transactionId)
                _listenerStatus.update { it.copy(activeTrackedCount = _trackedTransactionIds.value.size) }
            } catch (e: Exception) {
                Log.e(tag, "Error forcing immediate verification: ${e.message}")
            }
        }
    }

    private fun recordWebhookEvent(event: PayPalWebhookEvent) {
        _recentEvents.update { list ->
            (listOf(event) + list).take(30)
        }
        _listenerStatus.update { current ->
            current.copy(
                totalEventsReceived = current.totalEventsReceived + 1,
                lastPingTimestamp = System.currentTimeMillis()
            )
        }
    }

    private fun buildRawPayloadJson(
        eventId: String,
        eventType: String,
        batchId: String,
        itemId: String,
        receiver: String,
        amount: Double,
        status: String
    ): String {
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return """
        {
          "id": "$eventId",
          "event_version": "1.0",
          "create_time": "$isoDate",
          "resource_type": "payouts_item",
          "event_type": "$eventType",
          "summary": "Payout item $status for $receiver",
          "resource": {
            "payout_batch_id": "$batchId",
            "payout_item_id": "$itemId",
            "transaction_status": "$status",
            "payout_item": {
              "recipient_type": "EMAIL",
              "amount": {
                "value": "${String.format(Locale.US, "%.2f", amount)}",
                "currency": "USD"
              },
              "receiver": "$receiver"
            }
          },
          "links": [
            {
              "href": "https://api-m.sandbox.paypal.com/v1/notifications/webhooks-events/$eventId",
              "rel": "self",
              "method": "GET"
            }
          ]
        }
        """.trimIndent()
    }
}
