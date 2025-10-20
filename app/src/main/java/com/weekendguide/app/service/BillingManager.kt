package com.weekendguide.app.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Handles in-app purchases and subscriptions using Google Play Billing.
 * Supports one-time purchases (consumables) and recurring subscriptions.
 */
class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    /** Indicates whether the last purchase was successful. */
    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess

    /** Stores the type of the last successful purchase (INAPP or SUBS). */
    private val _lastPurchaseType = MutableStateFlow<String?>(null)
    val lastPurchaseType: StateFlow<String?> = _lastPurchaseType

    /** Stores the token of the last successful subscription. */
    private val _lastPurchaseToken = MutableStateFlow<String?>(null)
    val lastPurchaseToken: StateFlow<String?> = _lastPurchaseToken

    init {
        startConnection()
        checkAndConsumePendingPurchases()
    }

    /** Resets the purchase success flag (after handling it in the UI). */
    fun resetPurchaseFlag() {
        _purchaseSuccess.value = false
    }

    /** Establishes a connection with Google Play Billing service. */
    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Billing service disconnected — it will reconnect automatically.
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // Called when the setup process is completed.
                // You may check the result if needed, but no action is required here.
            }
        })
    }

    /** Starts a one-time product purchase flow (consumable). */
    fun purchaseOneTimeProduct(activity: Activity, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                productDetailsResult.productDetailsList.isNotEmpty()
            ) {
                val productDetails = productDetailsResult.productDetailsList[0]

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    /** Starts a subscription purchase flow. */
    fun purchaseSubscription(activity: Activity, productId: String) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                productDetailsResult.productDetailsList.isNotEmpty()
            ) {
                val productDetails = productDetailsResult.productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    ?: return@queryProductDetailsAsync

                val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    /** Called when the billing flow is finished or updated. */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                when (purchase.products.firstOrNull()?.let { getProductType(it) }) {
                    BillingClient.ProductType.SUBS -> handleSubscription(purchase)
                    BillingClient.ProductType.INAPP -> handleOneTimePurchase(purchase)
                }
            }
        }
    }

    /** Handles one-time product purchase (consumable). */
    private fun handleOneTimePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _lastPurchaseType.value = BillingClient.ProductType.INAPP

            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _purchaseSuccess.value = true
                }
            }
        }
    }

    /** Handles subscription purchase and acknowledges it if required. */
    private fun handleSubscription(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            _lastPurchaseType.value = BillingClient.ProductType.SUBS
            _lastPurchaseToken.value = purchase.purchaseToken

            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgeParams) { ackResult ->
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _purchaseSuccess.value = true
                    _lastPurchaseToken.value = purchase.purchaseToken
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // The subscription is already acknowledged
            _purchaseSuccess.value = true
        }
    }

    /**
     * Validates an existing subscription token at app startup.
     * Calls onResult(true) if the token is active, false otherwise.
     */
    fun validateSavedSubscriptionToken(savedToken: String?, onResult: (Boolean) -> Unit) {
        if (savedToken.isNullOrEmpty()) {
            onResult(false)
            return
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isActive = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            it.purchaseToken == savedToken
                }
                onResult(isActive)
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Consumes all pending one-time purchases that were not completed previously.
     * This allows the user to repurchase the same product again.
     */
    private fun checkAndConsumePendingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.consumeAsync(consumeParams) { result, _ ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            _purchaseSuccess.value = true
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines product type based on product ID prefix.
     * Example: IDs starting with "unlock_" are treated as one-time products.
     */
    private fun getProductType(productId: String): String {
        return if (productId.startsWith("unlock_")) BillingClient.ProductType.INAPP
        else BillingClient.ProductType.SUBS
    }
}
