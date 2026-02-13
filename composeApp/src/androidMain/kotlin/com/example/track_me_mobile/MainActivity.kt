package com.example.track_me_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.track_me_mobile.features.teams.presentation.TeamCreateScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TeamCreateScreen(
                onBackClick = {
                    // Здесь логика выхода из приложения или перехода на главный экран
                    // Если вы используете ComponentActivity:
                    finish()
                },
                onNavigateToInfo = { teamData ->
                    // Здесь будет логика перехода на TeamInfoScreen(data = teamData)
                    // Пока можно просто вывести в лог для проверки
                    println("Создана команда: ${teamData.stream}")
                }
            )
        }
    }
}