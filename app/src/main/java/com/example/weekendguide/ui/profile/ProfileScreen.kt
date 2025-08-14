package com.example.weekendguide.ui.profile

import android.content.Intent
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
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.weekendguide.BuildConfig
import com.example.weekendguide.Constants.EMAIL
import com.example.weekendguide.Constants.LEGAL_DOCS_URL
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.data.model.UserData
import com.example.weekendguide.viewmodel.ProfileViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserData,
    translateViewModel: TranslateViewModel,
    profileViewModel: ProfileViewModel,
    themeViewModel: ThemeViewModel,
    editProfile: () -> Unit,
    showSubscriptionBanner: @Composable () -> Unit,
    showLocationPanel: @Composable () -> Unit,
    showNavigationBar: @Composable () -> Unit,
    showStoreBanner: @Composable () -> Unit,
    showTopAppBar: @Composable () -> Unit,
) {

    val context = LocalContext.current
    var showSignOutDialog by remember { mutableStateOf(false) }
    val toastMessage by profileViewModel.toastMessage.collectAsState()

    // --- STATE FROM UserData ---
    val currentLanguage = userData.language ?: "en"
    val currentTheme = userData.userThema ?: ""
    val currentUnits = userData.userMeasurement ?: ""
    val notificationEnabled = userData.notification != false

    val email = userData.email ?: ""
    val userName = userData.displayName ?: email.substringBefore("@")
    val photoUrl = userData.photoUrl
    val userCity = userData.homeCity ?: ""

    // --- THEME SELECTION ---
    val themeValues = listOf("light", "dark", "system")
    val themeOptions = listOf(
        LocalizerUI.t("theme_options_light", currentLanguage),
        LocalizerUI.t("theme_options_dark", currentLanguage),
        LocalizerUI.t("theme_options_system", currentLanguage)
    )
    var selectedThemeIndex by remember {
        mutableStateOf(themeValues.indexOf(currentTheme).takeIf { it >= 0 } ?: 0)
    }
    var selectedTheme by remember { mutableStateOf(themeOptions[selectedThemeIndex]) }

    // --- LANGUAGE SELECTION ---
    val languageValues = listOf("en", "de", "ru")
    val languageOptions = listOf("English", "Deutsch", "Русский")
    var selectedLanguageIndex by remember {
        mutableStateOf(languageValues.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0)
    }
    var selectedLanguage by remember { mutableStateOf(languageOptions[selectedLanguageIndex]) }

    // --- UNITS SELECTION ---
    val unitsValues = listOf("km", "mi")
    val unitsOptions = listOf(
        LocalizerUI.t("units_options_metric", currentLanguage),
        LocalizerUI.t("units_options_imperial", currentLanguage)
    )
    var selectedUnitsIndex by remember {
        mutableStateOf(unitsValues.indexOf(currentUnits).takeIf { it >= 0 } ?: 0)
    }
    var selectedUnits by remember { mutableStateOf(unitsOptions[selectedUnitsIndex]) }

    // --- SHEET STATE ---
    var sheetVisible by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf<SettingsType?>(null) }
    fun openSheet(type: SettingsType) {
        sheetType = type
        sheetVisible = true
    }

    // --- EFFECTS ---
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(
                context,
                LocalizerUI.t(it, currentLanguage),
                Toast.LENGTH_LONG
            ).show()
            profileViewModel.clearToast()
        }
    }

    LaunchedEffect(currentTheme, currentLanguage) {
        val idx = themeValues.indexOf(currentTheme).takeIf { it >= 0 } ?: 0
        selectedThemeIndex = idx
        selectedTheme = themeOptions[idx]
    }

    LaunchedEffect(currentLanguage) {
        val idx = languageValues.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0
        selectedLanguageIndex = idx
        selectedLanguage = languageOptions[idx]
    }

    LaunchedEffect(currentUnits, currentLanguage) {
        val idx = unitsValues.indexOf(currentUnits).takeIf { it >= 0 } ?: 0
        selectedUnitsIndex = idx
        selectedUnits = unitsOptions[idx]
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

            //personal_data
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {

                        //avatar
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
                                    text = userName.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        //UserName
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.weight(1f))
                            {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 30.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = LocalizerUI.t("profile_edit", currentLanguage),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        editProfile()
                                        openSheet(SettingsType.PROFILE) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            //SubscriptionBanner
            item {
                showSubscriptionBanner()
                Spacer(Modifier.height(24.dp))
            }

            //Settings
            item {
                Text(LocalizerUI.t("settings", currentLanguage), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SettingRow(LocalizerUI.t("language", currentLanguage), selectedLanguage) { openSheet(SettingsType.LANGUAGE) }
                        SettingRow(LocalizerUI.t("theme", currentLanguage), selectedTheme) { openSheet(SettingsType.THEME) }
                        SettingRow(LocalizerUI.t("units", currentLanguage), selectedUnits) { openSheet(SettingsType.UNITS) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(LocalizerUI.t("notifications", currentLanguage),color = MaterialTheme.colorScheme.onBackground)
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { profileViewModel.setNotificationsEnabled(it) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            //StoreBanner
            item {
                showStoreBanner()
                Spacer(Modifier.height(24.dp))
            }

            //About
            item {
                val context = LocalContext.current

                Text(
                    text = LocalizerUI.t("about", currentLanguage),
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekend Guide",
                                fontSize = 24.sp,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "v.${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:$EMAIL".toUri()
                                    }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📧", fontSize = 32.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = LocalizerUI.t("feedback", currentLanguage),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = LocalizerUI.t("terms_of_use", currentLanguage),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, "${LEGAL_DOCS_URL}/terms-${currentLanguage}".toUri())
                                        context.startActivity(intent)
                                    }
                            )
                        }

                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                text = LocalizerUI.t("privacy_policy", currentLanguage),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, "${LEGAL_DOCS_URL}/privacy-policy-${currentLanguage}".toUri())
                                        context.startActivity(intent)
                                    }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            //SignOutAccount Button
            item {
                Column {
                    Button(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = LocalizerUI.t("sign_out_account", currentLanguage),
                            color = MaterialTheme.colorScheme.background
                        )
                    }

                    if (showSignOutDialog) {
                        AlertDialog(
                            onDismissRequest = { showSignOutDialog = false },
                            title = {
                                Text(
                                    LocalizerUI.t("sign_out_title", currentLanguage),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            text = { Text(LocalizerUI.t("sign_out_confirm", currentLanguage)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showSignOutDialog = false
                                    profileViewModel.signOut()
                                }) {
                                    Text(LocalizerUI.t("sign_out_button", currentLanguage))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSignOutDialog = false }) {
                                    Text(LocalizerUI.t("cancel", currentLanguage))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Settings Modal Sheet
        SettingsModalSheet(
            sheetType = sheetType,
            sheetVisible = sheetVisible,
            onDismiss = { sheetVisible = false },
            userName = userName,
            userEmail = email,
            userCity = userCity,
            selectedTheme = selectedTheme,
            themeOptions = themeOptions,
            themeValues = themeValues,
            selectedLanguage = selectedLanguage,
            languageOptions = languageOptions,
            languageValues = languageValues,
            selectedUnits = selectedUnits,
            unitsOptions = unitsOptions,
            unitsValues = unitsValues,
            profileViewModel = profileViewModel,
            translateViewModel = translateViewModel,
            themeViewModel = themeViewModel,
            currentLanguage = currentLanguage,
            showLocationPanel = { showLocationPanel() }
        )
    }
}


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
        Text(iconText, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary))
    }
}
