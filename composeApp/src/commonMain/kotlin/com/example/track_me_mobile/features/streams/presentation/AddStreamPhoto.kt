package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.download_icon
import trackmemobile.composeapp.generated.resources.personal_acc_icon

@Composable
fun AddStreamPhoto() {
    Box(
        modifier = Modifier
            .size(222.dp)
            .background(
                color = Color(0xFFE6D7FB),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Image(
            painter = painterResource(Res.drawable.personal_acc_icon),
            contentDescription = "Personal account",
            modifier = Modifier
                .size(135.dp)
                .align(Alignment.Center)
        )
        Image(
            painter = painterResource(Res.drawable.download_icon),
            contentDescription = "Upload photo",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 26.dp, top = 26.dp)
                .size(17.dp)
        )
    }
}