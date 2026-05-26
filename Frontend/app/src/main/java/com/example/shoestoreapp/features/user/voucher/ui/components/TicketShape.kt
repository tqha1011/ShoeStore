package com.example.shoestoreapp.features.user.voucher.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class TicketShape(
    private val cutoutRadius: Float = 16f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply { fillType = PathFillType.EvenOdd }
        path.addRect(Rect(0f, 0f, size.width, size.height))
        val centerY = size.height / 2f
        path.addOval(
            Rect(
                left = -cutoutRadius,
                top = centerY - cutoutRadius,
                right = cutoutRadius,
                bottom = centerY + cutoutRadius
            )
        )
        path.addOval(
            Rect(
                left = size.width - cutoutRadius,
                top = centerY - cutoutRadius,
                right = size.width + cutoutRadius,
                bottom = centerY + cutoutRadius
            )
        )
        return Outline.Generic(path)
    }
}

