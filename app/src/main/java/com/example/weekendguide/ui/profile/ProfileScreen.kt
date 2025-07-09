package com.example.weekendguide.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weekendguide.Constants.EMAIL
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.POI
import com.example.weekendguide.viewmodel.LoginViewModel
import com.example.weekendguide.viewmodel.ProfileViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

enum class SettingsType {
    THEME, LANGUAGE, UNITS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userPOIList: List<POI>,
    totalPOIList: List<POI>,
    showNavigationBar: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
    onLoggedOut: () -> Unit,
    themeViewModel: ThemeViewModel,
    loginViewModel: LoginViewModel,
    translateViewModel: TranslateViewModel,
    profileViewModel: ProfileViewModel,
    onOpenStore: () -> Unit,
) {

    var sheetVisible by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf<SettingsType?>(null) }

    fun openSheet(type: SettingsType) {
        sheetType = type
        sheetVisible = true
    }

    //Персональные данные
    val userInfo by loginViewModel.userData.collectAsState()
    val email = userInfo.email ?: ""
    val displayName = userInfo.displayName ?: ""
    val photoUrl = userInfo.photoUrl
    val name = displayName.ifBlank { email.substringBefore("@") }

    // ТЕМА
    val currentTheme by themeViewModel.theme.collectAsState()
    val themeOptions = listOf("Светлая", "Тёмная", "Настройки устройства")
    val themeValues = listOf("light", "dark", "system")
    var selectedThemeIndex by remember {
        mutableStateOf(themeValues.indexOf(currentTheme).takeIf { it >= 0 } ?: 0)
    }
    var selectedTheme by remember { mutableStateOf(themeOptions[selectedThemeIndex]) }

    LaunchedEffect(currentTheme) {
        val idx = themeValues.indexOf(currentTheme).takeIf { it >= 0 } ?: 0
        selectedThemeIndex = idx
        selectedTheme = themeOptions[idx]
    }

    // ЯЗЫК
    val currentLanguage by translateViewModel.language.collectAsState()
    val languagesOptions = listOf("Русский", "English", "Deutsch")
    val languageValues =  listOf("ru", "en", "de")
    var selectedLanguageIndex by remember {
        mutableStateOf(languageValues.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0)
    }
    var selectedLanguage by remember { mutableStateOf(languagesOptions[selectedLanguageIndex]) }

    LaunchedEffect(currentLanguage) {
        val idx = languageValues.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0
        selectedLanguageIndex = idx
        selectedLanguage = languagesOptions[idx]
    }


    // Единицы измерения
    val currentUnits by profileViewModel.units.collectAsState()
    val unitsOptions = listOf("Метрическая", "Британская имперская")
    val unitsValues = listOf("km", "mi")
    var selectedUnitsIndex by remember {
        mutableStateOf(unitsValues.indexOf(currentUnits).takeIf { it >= 0 } ?: 0)
    }
    var selectedUnits by remember { mutableStateOf(unitsOptions[selectedUnitsIndex]) }

    LaunchedEffect(currentUnits) {
        val idx = unitsValues.indexOf(currentUnits).takeIf { it >= 0 } ?: 0
        selectedUnitsIndex = idx
        selectedUnits = unitsOptions[idx]
    }

    // Уведомления вкл/выкл
    val notificationEnabled by profileViewModel.notification.collectAsState()

    // Прочее
    val totalPOIs = totalPOIList.size
    val visitedPOIs = userPOIList.size
    val exploredPercentage = if (totalPOIs > 0) {
        (visitedPOIs.toDouble() / totalPOIs * 100).roundToInt()
    } else {
        0
    }


    Scaffold(
        topBar = { showTopAppBar() },
        bottomBar = { showNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ——— Блок: Личные данные ———
            item {
                //Text("Личные данные", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = LocalizerUI.t("personal_data", currentLanguage),
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        // Иконка с первой буквой имени
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "User Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = name.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text("Name", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(12.dp))

                            Text("Email", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(email, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ——— Блок: Настройки ———
            item {
                Text("Настройки", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SettingRow("🌐 Язык", selectedLanguage) { openSheet(SettingsType.LANGUAGE) }
                        SettingRow("🌓 Экран", selectedTheme) { openSheet(SettingsType.THEME) }
                        SettingRow("📏 Измерения", selectedUnits) { openSheet(SettingsType.UNITS) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔 Уведомления")
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { profileViewModel.setNotificationsEnabled(it) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ——— Моя коллекция ———
            item {
                Text("Моя коллекция", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp)) // мягкий фон без внешних отступов
                        .padding(8.dp) // небольшой отступ вокруг карточки
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenStore()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🗺️", fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Откройте новые места!",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📍 Вам доступно")
                                Text(
                                    text = "$totalPOIs точек",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🎯 Исследовано")
                                Text(
                                    text = "$exploredPercentage%",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Посмотреть новые коллекции...",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ——— Блок: О приложении ———
            item {
                val context = LocalContext.current

                Text("О приложении", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // — Заголовок с названием и версией —
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekend Guide",
                                fontSize = 24.sp,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "v1.0.0",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // — Автор —
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83D\uDC64", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Разработчик: SSmoliagin", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(12.dp))

                        // — Обратная связь —
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$EMAIL")
                                    }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📧", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Обратная связь", color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(Modifier.height(16.dp))

                        // — Политика конфиденциальности —
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TODO: открыть ссылку */ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Условия использования и Политика конфиденциальности", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ——— Выйти / Удалить аккаунт ———
            item {
                AccountActionsSection(onLoggedOut, profileViewModel)
            }
        }

        // ⬇️ Вызов шторки
        if (sheetVisible && sheetType != null) {
            val (title, options, selected, onSelect) = when (sheetType) {
                SettingsType.THEME -> Quad(
                    "Тема", themeOptions, selectedTheme
                ) { selected: String ->
                    selectedTheme = selected
                    val idx = themeOptions.indexOf(selected)
                    themeViewModel.setTheme(themeValues[idx])
                    sheetVisible = false
                }

                SettingsType.LANGUAGE -> Quad(
                    "Язык", languagesOptions, selectedLanguage
                ) { selected: String ->
                    selectedLanguage = selected
                    val idx = languagesOptions.indexOf(selected)
                    translateViewModel.setLanguage(languageValues[idx])
                    sheetVisible = false
                }

                SettingsType.UNITS -> Quad(
                    "Единицы измерения", unitsOptions, selectedUnits
                ) { selected: String ->
                    selectedUnits = selected
                    val idx = unitsOptions.indexOf(selected)
                    profileViewModel.setUserMeasurement(unitsValues[idx])
                    sheetVisible = false
                }

                else -> return@Scaffold
            }

            SettingsBottomSheet(
                sheetTitle = title,
                options = options,
                selectedOption = selected,
                onOptionSelected = onSelect,
                onDismissRequest = { sheetVisible = false }
            )
        }
    }
}

@Composable
private fun <T> Quad(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
): Quadruple<String, List<T>, T, (T) -> Unit> = Quadruple(title, options, selected, onSelect)

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun SettingRow(
    iconText: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(iconText, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    sheetTitle: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEach { option ->
                val isSelected = option == selectedOption
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOptionSelected(option)
                            onDismissRequest()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            onOptionSelected(option)
                            onDismissRequest()
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(option)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AccountActionsSection(
    onLoggedOut: () -> Unit,
    profileViewModel: ProfileViewModel,
) {
    val context = LocalContext.current
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column {
        Button(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "⚠ Удалить аккаунт",
            color = Color.Red,
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                showDeleteDialog = true
            }
        )

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text("Выйти из аккаунта?") },
                text = { Text("Вы уверены, что хотите выйти?") },
                confirmButton = {
                    TextButton(onClick = {
                        showSignOutDialog = false
                        profileViewModel.signOut {
                            onLoggedOut()
                        }
                    }) {
                        Text("Выйти")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить аккаунт?") },
                text = { Text("Вы уверены, что хотите безвозвратно удалить свой аккаунт?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        profileViewModel.deleteAccount { success ->
                            if (success) {
                                Toast.makeText(context, "Аккаунт удалён", Toast.LENGTH_SHORT).show()
                                onLoggedOut()
                            } else {
                                Toast.makeText(context, "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Удалить", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}
