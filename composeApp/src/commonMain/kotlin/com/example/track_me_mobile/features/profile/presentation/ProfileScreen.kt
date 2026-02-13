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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.features.profile.presentation.components.*

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    phone: String,
    telegram: String,
    onNavigateToEdit: () -> Unit
) {
    Scaffold(
        topBar = { ProfileTopHeader() },
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
                text = "Личный кабинет",
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
            Text("Администратор", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(25.dp))

            ProfileStaticRow(name)
            ProfileStaticRow(email)
            ProfileStaticRow(phone)
            ProfileStaticRow(telegram)

            Spacer(modifier = Modifier.height(35.dp))

            Button(
                onClick = { /* Логика карточек */ },
                modifier = Modifier.fillMaxWidth(0.80f).height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
            ) {
                Text("Карточки команд", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Логика отчета */ }) {
                Text("Загрузить отчет", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(onClick = onNavigateToEdit) {
                Text("Редактировать", color = TrackMePurple, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileStaticRow(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(42.dp),
        color = TrackMePurpleLight.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = TrackMePurple, fontSize = 16.sp)
        }
    }
}