package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.YellowLight

@Composable
fun WarningBanner(
    isOverBudget: Boolean,
    isNegativeBalance: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOverBudget && !isNegativeBalance) return

    Column(modifier = modifier.fillMaxWidth()) {
        if (isOverBudget) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(YellowLight)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "সতর্কতা",
                        tint = WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "সতর্কতা: আপনার মোট খরচ নির্ধারিত মাসিক বাজেট লক্ষ্য ছাড়িয়ে গেছে!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WarningOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        if (isNegativeBalance) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DangerRedLight)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "সতর্কতা",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "সতর্কতা: আপনার অবশিষ্ট ব্যালেন্স ঋণাত্মক (ঘাটতি দেখা দিয়েছে)!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
