package com.example.moneylyfinance
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneylyfinance.copilot.CopilotPlugin
import com.example.moneylyfinance.ui.theme.MoneylyFinanceTheme
import com.example.moneylyfinance.ui.theme.SageCategoryColors
import com.example.moneylyfinance.ui.theme.SagePrimaryMuted
import com.example.moneylyfinance.ui.theme.SageProgressTrack
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
enum class FinanceScreen(val title: String) {
    DASHBOARD("Dashboard"),
    ADD_EXPENSE("Add Expense"),
    SAVED_EXPENSES("Saved Expenses"),
    DATA_SYNC("Data Sync"),
    BACKEND("Backend Management")
}
private val CATEGORY_OPTIONS = listOf("Food", "Transport", "Shopping", "Bills", "Other")
class MainActivity : ComponentActivity() {
    private lateinit var database: FinanceDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FinanceDatabase.getDatabase(applicationContext)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            MoneylyFinanceTheme {
                val expenseDao = database.expenseDao()
                val budgetDao = database.budgetDao()
                val financeViewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(expenseDao)
                )
                var selectedScreen by remember {
                    mutableStateOf(FinanceScreen.DASHBOARD)
                }
                var expenseToEdit by remember {
                    mutableStateOf<ExpenseEntity?>(null)
                }
                val drawerState = rememberDrawerState(
                    initialValue = DrawerValue.Closed
                )
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Moneyly Finance",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(20.dp)
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FinanceScreen.entries.forEach { screen ->

                                NavigationDrawerItem(
                                    label = { Text(text = screen.title) },
                                    selected = selectedScreen == screen,
                                    onClick = {
                                        selectedScreen = screen
                                        scope.launch {
                                            drawerState.close()
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            //Top nav bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical = 8.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "☰",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Text(
                                    text = selectedScreen.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }

                            when (selectedScreen) {

                                FinanceScreen.DASHBOARD -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        FinanceDashboard(
                                            modifier = Modifier.fillMaxWidth(),
                                            expenseDao = expenseDao,
                                            budgetDao = budgetDao,
                                            financeViewModel = financeViewModel
                                        )

                                        SpendingPredictionSection(expenseDao = expenseDao)

                                        // Room for the floating copilot button.
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }


                                FinanceScreen.ADD_EXPENSE -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(
                                                rememberScrollState()
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        ExpenseInput(
                                            expenseDao = expenseDao,
                                            expenseToEdit = expenseToEdit,
                                            onEditComplete = {
                                                expenseToEdit = null
                                                selectedScreen =
                                                    FinanceScreen.SAVED_EXPENSES
                                            }
                                        )
                                    }
                                }

                                FinanceScreen.SAVED_EXPENSES -> {
                                    SavedExpenses(
                                        expenseDao = expenseDao,
                                        onEditClick = { expense ->
                                            expenseToEdit = expense
                                            selectedScreen =
                                                FinanceScreen.ADD_EXPENSE
                                        }
                                    )
                                }

                                FinanceScreen.DATA_SYNC -> {
                                    DataSyncScreen(
                                        expenseDao = expenseDao
                                    )
                                }

                                FinanceScreen.BACKEND -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        BackendManagementCard()
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        CopilotPlugin.attach(this)
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoneylyFinanceTheme {
        Greeting("Android")
    }
}

@Composable
fun FinanceDashboard(
    modifier: Modifier = Modifier,
    expenseDao: ExpenseDao,
    budgetDao: BudgetDao,
    financeViewModel: FinanceViewModel
)
{
    val coroutineScope = rememberCoroutineScope()

    val expenses by financeViewModel.expenses.collectAsState(
        initial = emptyList()
    )
    val savedBudget by budgetDao.getBudget().collectAsState(initial = null)
    val totalExpenses by financeViewModel.totalExpenses.collectAsState(
        initial = 0.0
    )
    val monthlyExpenses by financeViewModel.monthlyExpenses.collectAsState(
        initial = 0.0
    )

    var budgetInput by remember { mutableStateOf("") }

    val remainingBudget =
        savedBudget?.monthlyLimit?.minus(monthlyExpenses)

    val categoryTotals = expenses
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SageCard(containerColor = MaterialTheme.colorScheme.primary) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Monthly budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SagePrimaryMuted
                )
                Text(
                    savedBudget?.let {
                        "RM ${"%.2f".format(it.monthlyLimit)}"
                    } ?: "Not set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SagePrimaryMuted
                )
            }

            if (remainingBudget != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        ) {
                            append("RM ${"%.2f".format(remainingBudget)}")
                        }
                        withStyle(
                            SpanStyle(fontSize = 14.sp, color = SagePrimaryMuted)
                        ) {
                            append("  remaining")
                        }
                    },
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    "Set a budget to track your spending.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            val limit = savedBudget?.monthlyLimit
            if (limit != null && limit > 0.0) {
                val used = (monthlyExpenses / limit).toFloat().coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SageProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(used)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SageTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = "Monthly budget (RM)",
                    modifier = Modifier.weight(1f)
                )

                SageTonalButton(
                    text = "Save Budget",
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            coroutineScope.launch {
                                budgetDao.saveBudget(
                                    BudgetEntity(monthlyLimit = amount)
                                )
                                budgetInput = ""
                            }
                        }
                    },
                    modifier = Modifier.height(56.dp)
                )
            }
        }

        //Monthly overview
        SageCard(spacing = 0.dp) {
            SummaryRow(label = "Income", value = "RM 3,000.00")

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SummaryRow(
                label = "Expenses",
                value = "RM ${"%.2f".format(totalExpenses)}",
                valueColor = MaterialTheme.colorScheme.error
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total balance",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "RM 2,500.00",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        //Category breakdown
        SageCard {
            Text(
                "Spending by category",
                style = MaterialTheme.typography.titleSmall
            )

            if (categoryTotals.isEmpty()) {
                Text(
                    "Your category spending will appear here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val sorted = categoryTotals.entries.sortedByDescending { it.value }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    sorted.forEachIndexed { index, entry ->
                        Box(
                            modifier = Modifier
                                .weight(entry.value.toFloat().coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(
                                    SageCategoryColors[index % SageCategoryColors.size]
                                )
                        )
                    }
                }

                Column {
                    sorted.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        SageCategoryColors[index % SageCategoryColors.size]
                                    )
                            )
                            Text(
                                entry.key,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp)
                            )
                            Text("RM ${"%.2f".format(entry.value)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, color = valueColor)
    }
}

@Composable
fun MonthlySummary(){

}

@Composable
fun ExpenseInput(
    expenseDao: ExpenseDao,
    expenseToEdit: ExpenseEntity? = null,
    onEditComplete: () -> Unit = {}
)
{
    var description by remember(expenseToEdit?.id){
        mutableStateOf(expenseToEdit?.description ?: "")
    }

    var amount by remember(expenseToEdit?.id) {
        mutableStateOf(
            expenseToEdit?.amount?.toString() ?: ""
        )
    }
    var category by remember(expenseToEdit?.id) {
        mutableStateOf(expenseToEdit?.category ?: "Other")
    }
    var categoryMenuExpanded by remember {
        mutableStateOf(false)
    }

    var aiLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var selectedDate by remember(expenseToEdit?.id) {
        mutableStateOf(
            expenseToEdit?.date
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())
        )
    }

    val isProblem = listOf("Please", "Enter", "failed", "Could not")
        .any { message.contains(it) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    )
    {
        SageTextField(
            value = description,
            onValueChange = { description = it },
            label = "Expense description",
            modifier = Modifier.fillMaxWidth()
        )

        SageTonalButton(
            text = if (aiLoading) "Suggesting..." else "Suggest Category with AI",
            enabled = !aiLoading,
            onClick = {
                val cleanDescription = description.trim()

                if (cleanDescription.isBlank()) {
                    message = "Enter a description first."
                } else {
                    coroutineScope.launch {
                        aiLoading = true

                        try {
                            val response = RetrofitClient.api.predictCategory(
                                AiCategoryRequest(cleanDescription)
                            )

                            category = response.category.toString()

                            message = "AI suggested: ${response.category}"
                        } catch (e: Exception) {
                            message = "AI suggestion failed. Check backend connection."
                        } finally {
                            aiLoading = false
                        }
                    }
                }
            }
        )

        SageTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount (RM)",
            textStyle = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SageTile(
                label = "Date",
                value = selectedDate,
                modifier = Modifier.weight(1f),
                onClick = {
                    val calendar = Calendar.getInstance()

                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(selectedDate)
                        ?.let { calendar.time = it }

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                SageTile(
                    label = "Category",
                    value = category,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        categoryMenuExpanded = !categoryMenuExpanded
                    }
                )

                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = {
                        categoryMenuExpanded = false
                    }
                ) {
                    CATEGORY_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isProblem) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }

        SagePrimaryButton(
            text = if (expenseToEdit == null) "Add Expense"
            else "Update Expense",
            onClick = {
                val cleanDescription = description.trim()
                val parsedAmount = amount.toDoubleOrNull()

                if (cleanDescription.isBlank())
                {
                    message = "Please enter an expense description."
                }
                else if (parsedAmount == null || parsedAmount <= 0.0)
                {
                    message = "Please enter a valid amount greater than zero."
                }
                else
                {

                    val expense = if (expenseToEdit == null) {

                        ExpenseEntity(
                            description = cleanDescription,
                            amount = parsedAmount,
                            date = selectedDate,
                            category = category
                        )
                    } else {
                        //Edit mode
                        expenseToEdit.copy(
                            description = cleanDescription,
                            amount = parsedAmount,
                            date = selectedDate,
                            category = category
                        )
                    }


                    coroutineScope.launch {
                        try
                        {
                            if (expenseToEdit == null)
                            {

                                val newId = expenseDao.insertExpense(expense)

                                message = "Saved locally. Syncing..."

                                val syncResult = ExpenseSyncManager(expenseDao)
                                    .syncExpenseById(newId.toInt())

                                message = if (syncResult == "Synced successfully.") {
                                    "Saved locally and synced: $cleanDescription - RM $parsedAmount"
                                } else {
                                    "Saved locally. $syncResult"
                                }

                            }
                            else
                            {
                                expenseDao.updateExpense(expense)
                                expenseDao.markExpenseAsPending(expense.id)
                                message = "Expense updated locally. Sync pending."
                                onEditComplete()
                            }

                            description = ""
                            amount = ""
                        }
                        catch (e: Exception)
                        {
                            message = "Could not save expense. Please try again."
                        }
                    }

                }
            }
        )
    }

}

@Composable
fun SavedExpenses(
    expenseDao: ExpenseDao,
    onEditClick: (ExpenseEntity) -> Unit
)
{
    var selectedDate by remember { mutableStateOf("") }
    val expenses by (
            if (selectedDate.isBlank())
            {
                expenseDao.getAllExpenses()
            }
            else
            {
                //Sshow only matching date expenses
                expenseDao.getExpensesByDate(selectedDate)
            }
            ).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SageTonalButton(
                text = if (selectedDate.isBlank()) "Choose date"
                else "Selected date: $selectedDate",
                onClick = {
                    val today = java.util.Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            //Calendar months
                            selectedDate = String.format(
                                java.util.Locale.getDefault(),
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                            )
                        },
                        today.get(java.util.Calendar.YEAR),
                        today.get(java.util.Calendar.MONTH),
                        today.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                }
            )

            if (selectedDate.isNotBlank())
            {
                TextButton(
                    onClick = {
                        selectedDate = ""
                    }
                ) {
                    Text("Clear date filter")
                }
            }
        }

        if (expenses.isEmpty())
        {
            if (selectedDate.isNotBlank()) {
                Text(
                    text = "No expenses found for $selectedDate.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No expenses saved yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else
        {
            SageCard(spacing = 0.dp) {
                expenses.forEachIndexed { index, expense ->

                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            // Display the date saved in the database.
                            Text(
                                text = "${expense.date} · ${expense.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RM ${
                                    String.format("%.2f", expense.amount)
                                }",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Row {
                                TextButton(
                                    onClick = {
                                        onEditClick(expense)
                                    }
                                ) {
                                    Text("Edit")
                                }

                                TextButton(
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                if (expense.serverId != null) {
                                                    val response = RetrofitClient.api
                                                        .deleteExpense(expense.serverId)

                                                    if (!response.isSuccessful) {
                                                        return@launch
                                                    }
                                                }
                                                expenseDao.deleteExpense(expense.id)
                                            }
                                            catch (e: Exception)
                                            {
                                                //Keep local expense
                                            }
                                        }
                                    }
                                ) {
                                    Text("Delete")
                                }
                            }
                        }

                    }

                }
            }
        }

        // Room for the floating copilot button.
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun BackendConnectionTest()
{
    var result by remember {
        mutableStateOf("Not tested yet")
    }
    var loading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Column {
        Text("Backend test: $result")

        Button(
            onClick = {
                scope.launch {
                    loading = true

                    result = try {
                        val expenses = RetrofitClient.api.getExpenses()
                        "Connected! Found ${expenses.size} expenses."
                    }
                    catch (e: Exception) {
                        android.util.Log.e(
                            "BackendTest",
                            "API request failed",
                            e
                        )
                        "Failed: ${e.javaClass.simpleName}: ${e.message}"
                    }

                    loading = false
                }
            },
            enabled = !loading
        ) {
            Text(
                if (loading) "Connecting..."
                else "Test backend connection"
            )
        }
    }
}


@Composable
fun BackendExpensesSection(
    editingExpense: ApiExpense?,
    onEditExpense: (ApiExpense) -> Unit,
    refreshKey: Int
)
{
    var expenses by remember {
        mutableStateOf<List<ApiExpense>>(emptyList())
    }
    var message by remember {
        mutableStateOf("Tap below to load backend expenses.")
    }
    var loading by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) {
            try {
                expenses = RetrofitClient.api.getExpenses()
                message = "Loaded ${expenses.size} backend expenses."
            } catch (e: Exception) {
                message = "Could not refresh expenses: ${e.message}"
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Backend Expenses",
            style = MaterialTheme.typography.titleSmall
        )

        SageTonalButton(
            text = if (loading) "Loading..."
            else "Load Backend Expenses",
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    message = "Loading expenses..."

                    try {
                        // Fetch expenses from Spring Boot.
                        expenses = RetrofitClient.api.getExpenses()
                        message = "Loaded ${expenses.size} backend expenses."
                    } catch (e: Exception) {
                        message = "Could not load backend expenses: ${e.message}"
                    }

                    loading = false
                }
            }
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Display each expense returned by the API.
        expenses.forEach { expense ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${expense.description} — RM ${
                        "%.2f".format(expense.amount)
                    } (${expense.category})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        onEditExpense(expense)
                    }
                )
                {
                    Text("Edit")
                }

                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        scope.launch {
                            try {
                                RetrofitClient.api.deleteExpense(expense.id)
                                expenses = RetrofitClient.api.getExpenses()
                                message = "Deleted ${expense.description}."
                            } catch (e: Exception) {
                                message = "Delete failed: ${e.message}"
                            }
                        }
                    }
                )
                {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun CreateBackendExpenseSection(
    editingExpense: ApiExpense? = null,
    onCancelEdit: () -> Unit = {}
)
{
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editingExpense)
    {
        if (editingExpense != null) {
            description = editingExpense.description
            amount = editingExpense.amount.toString()
            category = editingExpense.category
            message = "Editing ${editingExpense.description}"
        }
    }

    //Fields sit on a white card here
    val fieldColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Create Backend Expense",
            style = MaterialTheme.typography.titleSmall
        )

        SageTextField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            containerColor = fieldColor,
            modifier = Modifier.fillMaxWidth()
        )

        SageTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount (RM)",
            containerColor = fieldColor,
            modifier = Modifier.fillMaxWidth()
        )


        Box(modifier = Modifier.fillMaxWidth()) {
            SageTile(
                label = "Category",
                value = category,
                containerColor = fieldColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    categoryMenuExpanded = !categoryMenuExpanded
                }
            )

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = {
                    categoryMenuExpanded = false
                }
            ) {
                CATEGORY_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }


        SagePrimaryButton(
            enabled = !loading,
            text = when
            {
                loading -> "Saving..."
                editingExpense != null -> "Update Expense"
                else -> "Save to Backend"
            },
            onClick = {
                val amountValue = amount.toDoubleOrNull()

                if (description.isBlank() ||
                    amountValue == null ||
                    amountValue <= 0 ||
                    category.isBlank()
                )
                {
                    message = "Enter a description, valid amount, and category."
                }
                else {
                    scope.launch {
                        loading = true

                        try
                        {
                            val today = java.text.SimpleDateFormat(
                                "yyyy-MM-dd",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date())

                            val currentExpense = editingExpense

                            if (currentExpense != null)
                            {
                                val updated = RetrofitClient.api.updateExpense(
                                    currentExpense.id,
                                    ApiExpense(
                                        id = currentExpense.id,
                                        description = description.trim(),
                                        amount = amountValue,
                                        category = category.trim(),
                                        expenseDate = currentExpense.expenseDate
                                    )
                                )

                                message = "Updated: ${updated.description}"
                                onCancelEdit()
                            }
                            else
                            {
                                val created = RetrofitClient.api.createExpense(
                                    CreateExpenseRequest(
                                        description = description.trim(),
                                        amount = amountValue,
                                        category = category.trim(),
                                        expenseDate = today
                                    )
                                )

                                message = "Saved to backend: ${created.description}"
                            }

                            description = ""
                            amount = ""
                            category = "Food"
                        }
                        catch (e: Exception)
                        {
                            message = "Save failed: ${e.message}"
                        }

                        loading = false
                    }
                }
            }
        )

        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BackendManagementCard() {
    var expanded by remember { mutableStateOf(false) }

    var editingExpense by remember {
        mutableStateOf<ApiExpense?>(null)
    }
    var refreshKey by remember {
        mutableStateOf(0)
    }

    SageCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Backend Management",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Manage expenses stored on your Spring Boot server.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SagePrimaryButton(
            text = if (expanded) {
                "Hide Backend Tools"
            } else {
                "Show Backend Tools"
            },
            onClick = { expanded = !expanded }
        )

        if (expanded) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            BackendExpensesSection(
                editingExpense = editingExpense,
                onEditExpense = { expense ->
                    editingExpense = expense
                },
                refreshKey = refreshKey
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            CreateBackendExpenseSection(
                editingExpense = editingExpense,
                onCancelEdit = {
                    editingExpense = null
                    refreshKey++
                }
            )
        }
    }
}
@Composable
fun DataSyncScreen(expenseDao: ExpenseDao) {
    val scope = rememberCoroutineScope()
    var syncMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SageCard {
            Text(
                text = "Data Synchronisation",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Retry syncing locally saved expenses to your backend.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SagePrimaryButton(
                text = "Retry Pending Sync",
                onClick = {
                    scope.launch {
                        syncMessage = ExpenseSyncManager(expenseDao)
                            .syncPendingExpenses()
                    }
                }
            )

            if (syncMessage.isNotBlank())
            {
                Text(
                    text = syncMessage,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}