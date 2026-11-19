package com.example.moneylyfinance.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moneylyfinance.R

val Figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_bold, FontWeight.Bold)
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Figtree),
    displayMedium = base.displayMedium.copy(fontFamily = Figtree),
    displaySmall = base.displaySmall.copy(fontFamily = Figtree),
    headlineLarge = base.headlineLarge.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(
        fontFamily = Figtree,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = base.bodyMedium.copy(fontFamily = Figtree, letterSpacing = 0.sp),
    bodySmall = base.bodySmall.copy(fontFamily = Figtree, letterSpacing = 0.sp),
    labelLarge = base.labelLarge.copy(fontFamily = Figtree, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontFamily = Figtree),
    labelSmall = base.labelSmall.copy(fontFamily = Figtree)
)