

package com.example.weekendguide.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weekendguide.data.locales.LocalizerUI
import com.example.weekendguide.viewmodel.ProfileViewModel
import com.example.weekendguide.viewmodel.ThemeViewModel
import com.example.weekendguide.viewmodel.TranslateViewModel

enum class SettingsType {
    THEME, LANGUAGE, UNITS, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModalSheet(
    sheetType: SettingsType?,
    sheetVisible: Boolean,
    onDismiss: () -> Unit,
    userName: String,
    userEmail: String,
    userCity: String,
    selectedTheme: String,
    themeOptions: List<String>,
    themeValues: List<String>,
    selectedLanguage: String,
    languageOptions: List<String>,
    languageValues: List<String>,
    selectedUnits: String,
    unitsOptions: List<String>,
    unitsValues: List<String>,
    profileViewModel: ProfileViewModel,
    translateViewModel: TranslateViewModel,
    themeViewModel: ThemeViewModel,
    currentLanguage: String,
    showLocationPanel: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (!sheetVisible || sheetType == null) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        when (sheetType) {
            SettingsType.PROFILE -> {
                var name by remember { mutableStateOf(userName) }
                var email by remember { mutableStateOf(userEmail) }
                var city by remember { mutableStateOf(userCity) }
                var password by remember { mutableStateOf("") }
                var showPasswordField by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(LocalizerUI.t("profile_edit", currentLanguage),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground)

                    //Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(LocalizerUI.t("name", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        )
                    )

                    //email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (email == userEmail) showPasswordField = false
                        },
                        label = { Text(LocalizerUI.t("email", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        )
                    )

                    //
                    if (showPasswordField) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(LocalizerUI.t("current_password", currentLanguage)) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                    //Location
                    showLocationPanel()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    //SaveButton
                    Button(
                        onClick = {
                            if (email != userEmail && !showPasswordField) {
                                showPasswordField = true
                            } else {
                                if (name != userName) profileViewModel.setDisplayName(name)
                                if (email != userEmail) {
                                    if (password.isNotEmpty()) {
                                        profileViewModel.updateUserEmail(password, email)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            LocalizerUI.t("enter_current_password", currentLanguage),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(LocalizerUI.t("save", currentLanguage))
                    }

                    Spacer(Modifier.height(8.dp))

                    //delete_account
                    Text(
                        text = LocalizerUI.t("delete_account", currentLanguage),
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            showDeleteDialog = true
                        }
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = {
                                Text(
                                    LocalizerUI.t("delete_account_title", currentLanguage),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            text = {
                                Text(LocalizerUI.t("delete_account_confirm", currentLanguage))
                                   },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    profileViewModel.deleteAccount { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) LocalizerUI.t(
                                                "account_deleted",
                                                currentLanguage
                                            )
                                            else LocalizerUI.t(
                                                "account_delete_error",
                                                currentLanguage
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }) {
                                    Text(
                                        LocalizerUI.t("delete_button", currentLanguage),
                                        color = Color.Red
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text(LocalizerUI.t("cancel", currentLanguage))
                                }
                            }
                        )
                    }
                }

            }

            // --Settings--
            SettingsType.THEME, SettingsType.LANGUAGE, SettingsType.UNITS -> {
                val title: String
                val options: List<String>
                val selected: String
                val onSelect: (String) -> Unit

                when(sheetType) {
                    SettingsType.THEME -> {
                        title = "Theme"
                        options = themeOptions
                        selected = selectedTheme
                        onSelect = { sel ->
                            val idx = themeOptions.indexOf(sel)
                            themeViewModel.setTheme(themeValues[idx])
                            onDismiss()
                        }
                    }

                    SettingsType.LANGUAGE -> {
                        title = "Language"
                        options = languageOptions
                        selected = selectedLanguage
                        onSelect = { sel ->
                            val idx = languageOptions.indexOf(sel)
                            translateViewModel.setLanguage(languageValues[idx])
                            onDismiss()
                        }
                    }

                    SettingsType.UNITS -> {
                        title = "Units"
                        options = unitsOptions
                        selected = selectedUnits
                        onSelect = { sel ->
                            val idx = unitsOptions.indexOf(sel)
                            profileViewModel.setUserMeasurement(unitsValues[idx])
                            onDismiss()
                        }
                    }

                    else -> return@ModalBottomSheet
                }

                Column(Modifier.padding(16.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(16.dp))
                    options.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = option == selected, onClick = { onSelect(option) })
                            Spacer(Modifier.width(8.dp))
                            Text(option, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }
}

