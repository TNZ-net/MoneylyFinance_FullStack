package com.example.moneylyfinance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
private val MoneylySageColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageSurface,
    primaryContainer = SageTint,
    onPrimaryContainer = SagePrimary,

    secondary = SageSecondary,
    onSecondary = SageSurface,

    tertiary = SageRed,
    onTertiary = SageSurface,

    background = SageBackground,
    onBackground = SageInk,

    surface = SageSurface,
    onSurface = SageInk,
    surfaceVariant = SageField,
    onSurfaceVariant = SageMuted,

    surfaceContainerLowest = SageSurface,
    surfaceContainerLow = SageSheet,
    surfaceContainer = SageSheet,
    surfaceContainerHigh = SageTint,
    surfaceContainerHighest = SageField,

    outline = SageOutline,
    outlineVariant = SageLine,

    error = SageRed,
    onError = SageSurface,
    errorContainer = SageRedTint,
    onErrorContainer = SageRedDark
)

@Composable
fun MoneylyFinanceTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MoneylySageColorScheme,
        typography = Typography,
        content = content
    )
}
