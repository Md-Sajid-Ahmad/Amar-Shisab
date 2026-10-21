package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategories
import com.example.data.model.FinancialSummary
import com.example.data.model.TYPE_REFUND
import com.example.data.model.TransactionEntity
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.Slate600
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White
import com.example.ui.theme.YellowBright
import com.example.ui.theme.YellowGold
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MonthlyReportsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()

    var isGeneratingPdf by remember { mutableStateOf(false) }

    fun shareOrOpenPdf(pdfFile: File, actionType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = if (actionType == "share") {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "আমার হিসাব - ${DateUtils.formatYearMonthBengali(selectedYearMonth)} রিপোর্ট")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(intent, "পিডিএফ রিপোর্ট খুলুন বা শেয়ার করুন")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF খুলতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("monthly_reports_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            MonthSelectorHeader(
                currentYearMonth = selectedYearMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onSelectYearMonth = { viewModel.setSelectedYearMonth(it) }
            )
        }

        // PDF Action Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(YellowBright.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = YellowBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "মাসিক আর্থিক বিবরণী (A4 PDF)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            )
                            Text(
                                text = "স্বচ্ছ হিসাব ও অফিশিয়াল প্রিন্ট কপি ডাউনলোড করুন",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isGeneratingPdf) return@Button
                                isGeneratingPdf = true
                                coroutineScope.launch {
                                    try {
                                        val file = withContext(Dispatchers.IO) {
                                            PdfReportGenerator.generateReport(
                                                context = context,
                                                yearMonth = selectedYearMonth,
                                                summary = summary,
                                                transactions = transactions
                                            )
                                        }
                                        isGeneratingPdf = false
                                        shareOrOpenPdf(file, "open")
                                    } catch (e: Exception) {
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "PDF তৈরিতে সমস্যা: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_download_pdf"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowBright,
                                contentColor = Navy800
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Navy800,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF ডাউনলোড করুন", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (isGeneratingPdf) return@OutlinedButton
                                isGeneratingPdf = true
                                coroutineScope.launch {
                                    try {
                                        val file = withContext(Dispatchers.IO) {
                                            PdfReportGenerator.generateReport(
                                                context = context,
                                                yearMonth = selectedYearMonth,
                                                summary = summary,
                                                transactions = transactions
                                            )
                                        }
                                        isGeneratingPdf = false
                                        shareOrOpenPdf(file, "share")
                                    } catch (e: Exception) {
                                        isGeneratingPdf = false
                                        Toast.makeText(context, "PDF তৈরিতে সমস্যা: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_share_pdf"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, YellowBright),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "শেয়ার করুন",
                                tint = YellowBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Financial Overview Summary Table
        item {
            ReportFinancialSummaryCard(summary = summary)
        }

        // Category Breakdown Card
        item {
            CategoryBreakdownCard(transactions = transactions)
        }

        // Detailed Itemized Table Header
        item {
            Text(
                text = "বিস্তারিত লেনদেনের তালিকা (${DateUtils.toBengaliDigits(transactions.size)} টি)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy800
                )
            )
        }

        // Table header row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নং",
                        style = MaterialTheme.typography.labelMedium.copy(color = White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(30.dp)
                    )
                    Text(
                        text = "বিবরণ",
                        style = MaterialTheme.typography.labelMedium.copy(color = White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "ক্যাটাগরি",
                        style = MaterialTheme.typography.labelMedium.copy(color = White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = "পরিমাণ",
                        style = MaterialTheme.typography.labelMedium.copy(color = White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.width(85.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এই মাসে কোনো লেনদেন রেকর্ড নেই",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(transactions, key = { _, it -> it.id }) { index, tx ->
                val isRefund = tx.type == TYPE_REFUND
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (index == transactions.lastIndex) {
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    } else {
                        RoundedCornerShape(0.dp)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.toBengaliDigits(index + 1),
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                            modifier = Modifier.width(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Navy800
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "${DateUtils.formatDateBengali(tx.timestamp).take(12)} • ${DateUtils.formatTimeBengali(tx.timestamp)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                            )
                        }
                        Text(
                            text = if (isRefund) "রিফান্ড" else tx.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                            modifier = Modifier.width(100.dp),
                            maxLines = 1
                        )
                        val sign = if (isRefund) "+" else "-"
                        val color = if (isRefund) SuccessGreen else DangerRed
                        Text(
                            text = "$sign${DateUtils.formatTaka(tx.amount, withSymbol = false)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = color
                            ),
                            modifier = Modifier.width(85.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportFinancialSummaryCard(summary: FinancialSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_financial_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "আর্থিক সারসংক্ষেপ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy800
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ReportStatRow(label = "প্রারম্ভিক ব্যালেন্স (Starting Balance):", value = DateUtils.formatTaka(summary.startingBalance))
            ReportStatRow(label = "মাসিক বাজেট লক্ষ্য (Target Budget):", value = DateUtils.formatTaka(summary.targetBudget))
            ReportStatRow(label = "মোট খরচ (Total Expenses):", value = DateUtils.formatTaka(summary.totalExpenses), valueColor = DangerRed)
            ReportStatRow(label = "মোট রিফান্ড (Total Refunds):", value = DateUtils.formatTaka(summary.totalRefunds), valueColor = SuccessGreen)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ReportStatRow(
                label = "অবশিষ্ট ব্যালেন্স (Final Balance):",
                value = DateUtils.formatTaka(summary.actualRemainingBalance),
                valueColor = if (summary.actualRemainingBalance >= 0) Navy800 else DangerRed,
                isBold = true
            )
            ReportStatRow(
                label = "বাজেট ব্যবহারের হার:",
                value = "${DateUtils.toBengaliDigits(summary.budgetUsagePercent)}%",
                valueColor = if (summary.budgetUsagePercent <= 100) Navy800 else DangerRed,
                isBold = true
            )
        }
    }
}

@Composable
private fun ReportStatRow(
    label: String,
    value: String,
    valueColor: Color = Navy800,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isBold) Navy800 else Slate600,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = valueColor
            )
        )
    }
}

@Composable
private fun CategoryBreakdownCard(transactions: List<TransactionEntity>) {
    val expenseTransactions = transactions.filter { it.type != TYPE_REFUND }
    val totalExpense = expenseTransactions.sumOf { it.amount }

    val categoryBreakdown = expenseTransactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_breakdown_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Navy800,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ক্যাটাগরি অনুযায়ী খরচের বিভাজন",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
            }

            if (categoryBreakdown.isEmpty()) {
                Text(
                    text = "খরচের কোনো রেকর্ড পাওয়া যায়নি",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
            } else {
                categoryBreakdown.forEach { (catName, catAmount) ->
                    val meta = ExpenseCategories.getCategoryMeta(catName)
                    val percentage = if (totalExpense > 0L) {
                        ((catAmount.toDouble() / totalExpense.toDouble()) * 100).toInt()
                    } else 0
                    val fraction = (percentage / 100f).coerceIn(0f, 1f)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(meta.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Navy800
                                    )
                                )
                            }
                            Text(
                                text = "${DateUtils.formatTaka(catAmount)} (${DateUtils.toBengaliDigits(percentage)}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Navy800
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = meta.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
