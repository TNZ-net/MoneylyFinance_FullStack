package com.example.moneylyfinance.copilot

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.example.moneylyfinance.ui.theme.MoneylyFinanceTheme

/**
 * CopilotPlugin.attach(this)
 */
object CopilotPlugin {
    fun attach(activity: ComponentActivity) {
        activity.addContentView(
            ComposeView(activity).apply { setContent { MoneylyFinanceTheme { CopilotOverlay() } } },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }
}
