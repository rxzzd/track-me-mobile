package com.example.track_me_mobile.core.ui.components // Теперь он в core

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class ProfileMenuShape(val density: Density) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val d = density.density
            val cp = 16f * d
            val innerCp = 15f * d
            val bw = 58f * d
            val bh = 40f * d

            moveTo(0f, bh + cp)
            quadraticBezierTo(0f, bh, cp, bh)
            lineTo(size.width - bw - innerCp, bh)
            quadraticBezierTo(size.width - bw, bh, size.width - bw, bh - innerCp)
            lineTo(size.width - bw, cp)
            quadraticBezierTo(size.width - bw, 0f, size.width - bw + cp, 0f)
            lineTo(size.width - cp, 0f)
            quadraticBezierTo(size.width, 0f, size.width, cp)
            lineTo(size.width, size.height - cp)
            quadraticBezierTo(size.width, size.height, size.width - cp, size.height)
            lineTo(cp, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - cp)
            close()
        }
        return Outline.Generic(path)
    }
}