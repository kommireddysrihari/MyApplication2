package com.example.myapplication.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.local.ExpenseEntity
import com.example.myapplication.data.model.Currency
import com.example.myapplication.data.model.ExpenseCategory
import com.example.myapplication.data.scanner.ReceiptScannerHelper
import com.example.myapplication.ui.theme.BlueLight
import com.example.myapplication.ui.theme.BluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseDialog(
    expenseToEdit: ExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, currency: String, category: String, date: Long, notes: String, receiptUri: String?) -> Unit,
    onConvertCurrency: (amount: Double, currency: String, callback: (Double) -> Unit) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amountStr by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var selectedCurrency by remember { mutableStateOf(expenseToEdit?.currency ?: "INR") }
    var selectedCategory by remember { mutableStateOf(expenseToEdit?.category ?: ExpenseCategory.FOOD.title) }
    var selectedDateMillis by remember { mutableStateOf(expenseToEdit?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }
    var receiptUri by remember { mutableStateOf(expenseToEdit?.receiptUri) }

    var convertedInrPreview by remember { mutableStateOf<Double?>(expenseToEdit?.convertedAmountInr) }
    var isScanning by remember { mutableStateOf(false) }
    var scanNotice by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Image picker launcher for receipt scanner ML Kit
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            receiptUri = uri.toString()
            isScanning = true
            scanNotice = null
            ReceiptScannerHelper.scanReceiptImage(
                context = context,
                imageUri = uri,
                onSuccess = { extractedAmount, _ ->
                    isScanning = false
                    if (extractedAmount != null && extractedAmount > 0) {
                        amountStr = String.format(Locale.US, "%.2f", extractedAmount)
                        scanNotice = "Receipt scanned! Extracted amount: $extractedAmount autofilled."
                        Toast.makeText(context, "Amount $extractedAmount extracted from receipt!", Toast.LENGTH_SHORT).show()
                    } else {
                        scanNotice = "Receipt loaded. No clear total amount detected."
                    }
                },
                onError = { e ->
                    isScanning = false
                    scanNotice = "Failed to scan receipt text."
                    Toast.makeText(context, "Scanning error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Trigger currency conversion preview whenever amount or currency changes
    LaunchedEffect(amountStr, selectedCurrency) {
        val parsed = amountStr.toDoubleOrNull()
        if (parsed != null && parsed > 0) {
            if (selectedCurrency == "INR") {
                convertedInrPreview = parsed
            } else {
                onConvertCurrency(parsed, selectedCurrency) { converted ->
                    convertedInrPreview = converted
                }
            }
        } else {
            convertedInrPreview = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (expenseToEdit == null) "Add New Expense" else "Edit Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Receipt Scanner Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlueLight)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "Scan Receipt",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (receiptUri == null) "Scan Receipt Image" else "Receipt Attached",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Use Google ML Kit to auto-fill amount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else if (receiptUri != null) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Loaded", tint = BluePrimary)
                    }
                }

                if (scanNotice != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scanNotice!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Grocery, Taxi)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amount & Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Amount Input
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1.3f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )

                    // Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                focusedLabelColor = BluePrimary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            Currency.SUPPORTED_CURRENCIES.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text("${curr.symbol} ${curr.code} - ${curr.name}") },
                                    onClick = {
                                        selectedCurrency = curr.code
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Currency Conversion Live Preview
                if (convertedInrPreview != null && !selectedCurrency.equals("INR", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Converted",
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "Converted: ₹ %,.2f INR (via Frankfurter API)", convertedInrPreview),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.getAllCategories().forEach { category ->
                        val isSelected = selectedCategory.equals(category.title, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category.title },
                            label = { Text(category.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector Button
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateFormat.format(Date(selectedDateMillis)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Date", tint = BluePrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull()
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter an expense title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSave(title, parsedAmount, selectedCurrency, selectedCategory, selectedDateMillis, notes, receiptUri)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Expense", color = Color.White)
                    }
                }
            }
        }
    }

    // Material DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = BluePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
