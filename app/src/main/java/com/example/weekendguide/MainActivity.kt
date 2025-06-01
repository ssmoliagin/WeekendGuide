
package com.example.weekendguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.weekendguide.ui.navigation.AppNavigation
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContent {
            WeekendGuideTheme(darkTheme = false)  {
                AppNavigation()
            }
        }
    }
}


/*
// ПРОВЕРОЧНАЯ ВЕРСИЯ С ПРОСТОЙ ЗАГРУЗКОЙ И ЧТЕНИЕМ ФАЙЛА ИЗ FIREBASE
//
package com.example.weekendguide

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weekendguide.ui.theme.WeekendGuideTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.tasks.await


class MainActivity : ComponentActivity() {

    data class Country(
        @SerializedName("code") val code: String,
        @SerializedName("name_en") val nameEn: String,
        @SerializedName("name_de") val nameDe: String,
        @SerializedName("name_ru") val nameRu: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Явная инициализация Firebase, если не произошло автоматически
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseInit", "Firebase initialized: ${FirebaseApp.getInstance().name}")

        setContent {
            WeekendGuideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountriesScreen()
                }
            }
        }
    }

    @Composable
    fun CountriesScreen() {
        var countries by remember { mutableStateOf<List<Country>>(emptyList()) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                val storage = Firebase.storage
                val gsRef = storage.getReferenceFromUrl(
                    "gs://weekendguide-dfc8c.firebasestorage.app/data/places/countries.json" // ТОЛЬКО ПОЛНАЯ ССЫЛКА
                )
                val bytes = gsRef.getBytes(1024 * 1024).await()
                val json = String(bytes, Charsets.UTF_8)
                countries = Gson().fromJson(json, Array<Country>::class.java).toList()

                Log.d("Firebase", "Загружено стран: ${countries.size}")
                countries.forEach {
                    Log.d("Firebase", "${it.nameRu} (${it.code})")
                }

            } catch (e: Exception) {
                Log.e("Firebase", "Ошибка загрузки стран", e)
                error = "Ошибка загрузки данных"
            }
        }

        if (error != null) {
            Text(
                text = error ?: "Неизвестная ошибка",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(countries) { country ->
                    Text(
                        text = "${country.nameRu} (${country.code})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Divider()
                }
            }
        }
    }
}
*/