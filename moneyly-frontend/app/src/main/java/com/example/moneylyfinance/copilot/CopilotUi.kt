package com.example.moneylyfinance.copilot

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.moneylyfinance.FinanceDatabase
import kotlin.math.roundToInt

private class Msg(val text: String, val fromUser: Boolean)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotOverlay() {
    val db = FinanceDatabase.getDatabase(LocalContext.current)
    val expenseFlow = remember(db) { db.expenseDao().getAllExpenses() }
    val budgetFlow = remember(db) { db.budgetDao().getBudget() }
    val expenses by expenseFlow.collectAsState(initial = emptyList())
    val budget by budgetFlow.collectAsState(initial = null)

    val today = CopilotEngine.today()
    val tx = remember(expenses) {
        expenses.mapNotNull { e -> parseDay(e.date)?.let { Tx(it, e.amount, e.category, e.description) } }
    }
    val limit = budget?.monthlyLimit
    val cards = remember(tx, limit, today) { CopilotEngine.insights(tx, limit, today) }

    var open by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val msgs = remember { mutableStateListOf(Msg(CopilotEngine.GREETING, false)) }

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val range = with(LocalDensity.current) { (maxHeight - 180.dp).toPx() }
        val warns = cards.count { it.warn }

        // Floating button: sits on the right edge, drag it up/down out of the way of your content.
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 120.dp)
                .offset { IntOffset(0, dragY.roundToInt()) }
                .pointerInput(range) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        dragY = (dragY + drag.y).coerceIn(-range.coerceAtLeast(0f), 0f)
                    }
                }
        ) {
            FloatingActionButton(
                onClick = { open = true },
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Text("✦", style = MaterialTheme.typography.titleLarge) }
            if (warns > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) { Text("$warns") }
            }
        }
    }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            CopilotSheet(cards, msgs) { q ->
                msgs.add(Msg(q, true))
                msgs.add(Msg(CopilotEngine.answer(q, tx, limit, today), false))
            }
        }
    }
}

@Composable
private fun CopilotSheet(cards: List<Insight>, msgs: List<Msg>, onAsk: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    val list = rememberLazyListState()
    // 1 header + cards + messages: once the user chats, keep the newest message in view.
    LaunchedEffect(msgs.size) { if (msgs.size > 1) list.animateScrollToItem(cards.size + msgs.size) }

    fun send(q: String) {
        if (q.isNotBlank()) { onAsk(q.trim()); input = "" }
    }

    val colors = MaterialTheme.colorScheme

    Column(
        Modifier.fillMaxWidth().fillMaxHeight(0.8f).imePadding().padding(horizontal = 14.dp)
    ) {
        LazyColumn(Modifier.weight(1f), state = list, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Text(
                    "Copilot insights",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            items(cards) { c ->
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape)
                                .background(if (c.warn) colors.errorContainer else colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (c.warn) "!" else "✓",
                                color = if (c.warn) colors.onErrorContainer else colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(c.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                c.detail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            items(msgs) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(
                        shape = if (m.fromUser) RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
                        else RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp),
                        color = if (m.fromUser) colors.primary else colors.surface,
                        contentColor = if (m.fromUser) colors.onPrimary else colors.onSurface,
                        modifier = Modifier.padding(top = 2.dp).widthIn(max = 310.dp)
                    ) { Text(m.text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) }
                }
            }
        }

        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CopilotEngine.SUGGESTIONS.forEach { s ->
                AssistChip(
                    onClick = { send(s) },
                    label = { Text(s) },
                    shape = RoundedCornerShape(18.dp),
                    border = null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.primaryContainer,
                        labelColor = colors.primary
                    )
                )
            }
        }

        Row(
            Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Ask about your spending") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send(input) }),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Button(
                onClick = { send(input) },
                modifier = Modifier.width(84.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) { Text("Send", style = MaterialTheme.typography.labelLarge) }
        }
    }
}
