package com.example.weekendguide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionBanner(
    currentLanguage: String,
    isSubscription: Boolean,
    subscriptionViewModel: SubscriptionViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val subscriptionBenefitsVisible by subscriptionViewModel.subscriptionBenefitsVisible.collectAsState()

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        onClick = { showDialog = true }
    )
    {
        Column(Modifier.padding(1.dp))
        {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = if (isSubscription) LocalizerUI.t("active_subscription", currentLanguage)
                        else LocalizerUI.t("join_now", currentLanguage),
                        color = MaterialTheme.colorScheme.background)

                    Text(
                        text = "WeekendGuide PLUS!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary)
                }
                Icon(
                    imageVector = if (isSubscription) Icons.Default.Star
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (isSubscription) "isSubscription" else "Select",
                    tint = if (isSubscription) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (!isSubscription) {
        Text(
            LocalizerUI.t("learn_benefits", currentLanguage),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { subscriptionViewModel.toggleSubscriptionBenefitVisibility() }
        )

        if (subscriptionBenefitsVisible) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        LocalizerUI.t("subscription_benefits_title", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BenefitItem("🌍", LocalizerUI.t("benefit_all_regions", currentLanguage))
                    BenefitItem("📡", LocalizerUI.t("benefit_unlimited_radius", currentLanguage))
                    BenefitItem("⭐", LocalizerUI.t("benefit_double_points", currentLanguage))
                    BenefitItem("🤝", LocalizerUI.t("benefit_share_unlimited", currentLanguage))
                    BenefitItem("📂", LocalizerUI.t("benefit_gpx", currentLanguage))
                    BenefitItem("📴", LocalizerUI.t("benefit_offline", currentLanguage))

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = LocalizerUI.t("try_free", currentLanguage),
                            color = MaterialTheme.colorScheme.background,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }



    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    if (isSubscription) LocalizerUI.t("unsubscribe", currentLanguage)
                    else LocalizerUI.t("subscribe", currentLanguage),
                color = MaterialTheme.colorScheme.onBackground
                )
                    },
            text = {
                Text(
                    if (isSubscription) LocalizerUI.t("confirm_unsubscribe", currentLanguage)
                    else LocalizerUI.t("confirm_subscribe", currentLanguage)
                )
                   },

            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    subscriptionViewModel.setSubscriptionEnabled(!isSubscription)
                })
                { Text(if (isSubscription) LocalizerUI.t("unsubscribe", currentLanguage)
                else LocalizerUI.t("subscribe", currentLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(LocalizerUI.t("cancel", currentLanguage)) }
            }
        )
    }

}

@Composable
fun BenefitItem(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = emoji,
            modifier = Modifier.padding(end = 8.dp),
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}