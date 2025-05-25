package com.example.weekendguide.ui.region

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectRegionScreen(
    onNavigate: (String) -> Unit
) {
    val regions = listOf("Region 1", "Region 2", "Region 3") // замените на реальные регионы или загрузите из ViewModel

    var selectedRegion by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Выберите ваш регион", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(regions) { region ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRegion = region
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = region == selectedRegion,
                        onClick = { selectedRegion = region }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = region)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedRegion?.let {
                    // сохранить выбранный регион, например, в ViewModel или DataStore
                    onNavigate("main") // переход на главный экран
                }
            },
            enabled = selectedRegion != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Далее")
        }
    }
}
