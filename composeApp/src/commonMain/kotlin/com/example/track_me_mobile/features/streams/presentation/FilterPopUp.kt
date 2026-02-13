package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun FilterPopUp(
    showWindow: Boolean,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showWindow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            )
        }
        if (showWindow) {
            Popup(
                alignment = Alignment.TopCenter,
                properties = PopupProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    focusable = true
                )
            ) {
                Card(
                    modifier = Modifier
                        .width(328.dp)
                        .height(635.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCDAFF7)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                    }
                }
            }
        }
    }
}