package com.example.track_me_mobile.features.profile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.core.ui.theme.TrackMePurpleLight

@Composable
fun ProfileInfoRow(value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp)
            .height(42.dp),
        color = TrackMePurpleLight.copy(alpha = 0.7f),
        shape = RoundedCornerShape(50)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value,
                color = TrackMePurple,
                fontSize = 16.sp
            )
        }
    }
}