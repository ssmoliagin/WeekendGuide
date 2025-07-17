package com.example.weekendguide.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StoreBanner(
    totalPOIs: Int,
    exploredPercentage: Int,
    currentLanguage: String,
    onOpenStore: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
        onClick = { onOpenStore() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // Внутренние отступы
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\uD83D\uDDFA\uFE0F", fontSize = 36.sp) // Крупнее эмодзи
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Откройте новые места!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp // Чуть больше
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📍 Вам доступно",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = "$totalPOIs точек",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎯 Исследовано",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = "$exploredPercentage%",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "➡ Посмотреть новые коллекции",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            )
        }
    }
}

