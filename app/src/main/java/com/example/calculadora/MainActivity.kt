package com.example.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calculadora.ui.theme.CalculadoraTheme
import com.example.calculadora.ui.theme.DarkGreen
import com.example.calculadora.ui.theme.FinancialGreen
import com.example.calculadora.ui.theme.LightGreen
import java.util.Locale
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoanApp()
                }
            }
        }
    }
}

@Composable
fun LoanApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "input") {
        composable("input") { InputScreen(navController) }
        composable("result/{amount}/{term}/{rate}/{isYears}") { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            val term = backStackEntry.arguments?.getString("term")?.toDoubleOrNull() ?: 0.0
            val rate = backStackEntry.arguments?.getString("rate")?.toDoubleOrNull() ?: 0.0
            val isYears = backStackEntry.arguments?.getString("isYears")?.toBoolean() ?: true
            ResultScreen(navController, amount, term, rate, isYears)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var term by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var isYears by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinancialGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.loan_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text(stringResource(R.string.loan_term)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Toggle Buttons for Years/Months
                Row(
                    modifier = Modifier
                        .background(LightGreen, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isYears = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isYears) FinancialGreen else Color.Transparent,
                            contentColor = if (isYears) Color.White else DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(stringResource(R.string.years))
                    }
                    Button(
                        onClick = { isYears = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isYears) FinancialGreen else Color.Transparent,
                            contentColor = if (!isYears) Color.White else DarkGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(stringResource(R.string.months))
                    }
                }
            }

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text(stringResource(R.string.interest_rate)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            if (showError) {
                Text(
                    text = stringResource(R.string.error_empty),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (amount.isNotEmpty() && term.isNotEmpty() && rate.isNotEmpty()) {
                        showError = false
                        navController.navigate("result/$amount/$term/$rate/$isYears")
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.calculate), fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, amount: Double, term: Double, rate: Double, isYears: Boolean) {
    val months = if (isYears) term * 12 else term
    val monthlyRate = rate / 12 / 100
    
    val monthlyFee = if (rate == 0.0) {
        amount / months
    } else {
        (amount * monthlyRate * (1 + monthlyRate).pow(months)) / ((1 + monthlyRate).pow(months) - 1)
    }

    val totalPaid = monthlyFee * months
    val totalInterest = totalPaid - amount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.results), color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinancialGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResultCard(stringResource(R.string.monthly_fee), String.format(Locale.US, "%.2f", monthlyFee))
            ResultCard(stringResource(R.string.loan_amount), String.format(Locale.US, "%.2f", amount))
            ResultCard(stringResource(R.string.total_interest), String.format(Locale.US, "%.2f", totalInterest))
            ResultCard(stringResource(R.string.total_amount), String.format(Locale.US, "%.2f", totalPaid))

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinancialGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.back), fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ResultCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = label, color = DarkGreen, fontSize = 14.sp)
            Text(
                text = "$$value",
                color = FinancialGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}