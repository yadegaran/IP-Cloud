package com.tools.net

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FragmentFinderScreen(vm: ScannerViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- هدر صفحه ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("فرگمنت یاب حرفه‌ای", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("خروجی بازه‌ای مخصوص V2Ray", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- بخش دکمه‌های کنترل (اسکن و توقف) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.startDeepFragmentScan() },
                modifier = Modifier.weight(1f),
                enabled = !vm.isScanningg, // غیرفعال شدن دکمه هنگام اسکن
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (vm.isScanningg) "در حال اسکن..." else "شروع اسکن")
            }

            // نمایش دکمه توقف فقط در زمان اسکن
            AnimatedVisibility(visible = vm.isScanningg) {
                Button(
                    onClick = { vm.stopScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("توقف", color = Color.White)
                }
            }
        }

        // --- وضعیت پیشرفت و اطلاعات تست ---
        if (vm.isScanningg || vm.currentProgress > 0) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                LinearProgressIndicator(
                    progress = vm.currentProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (vm.isScanningg) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vm.currentTestInfo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (vm.isScanningg) MaterialTheme.colorScheme.primary else Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- لیست نتایج ---
        Text("نتایج یافت شده (برای کپی کلیک کنید):", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(vm.scanResults) { result ->
                FragmentResultItem(result) {
                    // کپی کردن به فرمت V2Ray
                    val copyText = "Length: ${result.lengthRange} | Interval: ${result.intervalRange}"
                    clipboardManager.setText(AnnotatedString(copyText))
                    Toast.makeText(context, "کپی شد!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun FragmentResultItem(result: FragmentResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // دایره نمایش پایداری
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        if (result.stability > 80) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("${result.stability}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("اندازه: ${result.lengthRange}", fontWeight = FontWeight.Bold)
                Text("فاصله: ${result.intervalRange}", fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${result.latency}ms", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                Text("پینگ", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}