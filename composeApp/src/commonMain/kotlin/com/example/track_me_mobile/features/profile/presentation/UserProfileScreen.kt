package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.*
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

data class UserProfileScreen(val username: String) : Screen {
    @Composable
    override fun Content() {
        val viewModel: UserProfileViewModel = koinInject { parametersOf(username) }
        val state = viewModel.state

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TrackMePurple)
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            else -> {
                UserProfileContent(
                    name = state.fullName,
                    email = state.email,
                    phone = state.phoneNumber,
                    telegram = "@${state.username}",
                    role = state.roles.firstOrNull() ?: "Пользователь"
                )
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    name: String,
    email: String,
    phone: String,
    telegram: String,
    role: String
) {
    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Профиль пользователя",
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                color = TrackMePurple,
                modifier = Modifier.padding(bottom = 35.dp)
            )

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TrackMePurpleLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = TrackMePurple
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text(role, color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(25.dp))

            ProfileStaticRow(name)
            ProfileStaticRow(email)
            ProfileStaticRow(phone)
            ProfileStaticRow(telegram)

            Spacer(modifier = Modifier.height(35.dp))
            
        }
    }
}