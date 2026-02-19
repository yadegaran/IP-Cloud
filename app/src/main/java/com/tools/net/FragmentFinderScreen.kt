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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
        // هدر صفحه
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("فرگمنت یاب پیشرفته", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("پیدا کردن نقطه کور فیلترینگ اپراتور", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // راهنمای خاموش بودن VPN
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("نکته: برای دقت اسکن، فیلترشکن را خاموش کنید.", fontSize = 11.sp, color = Color(0xFFE65100))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // دکمه‌های کنترل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.startDeepFragmentScan() },
                modifier = Modifier.weight(1f),
                enabled = !vm.isScanningg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("شروع اسکن")
            }

            if (vm.isScanningg) {
                Button(
                    onClick = { vm.stopScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("توقف", color = Color.White)
                }
            }
        }

        // پروگرس بار و وضعیت سلامت IP
        AnimatedVisibility(visible = vm.isScanningg || vm.currentProgress > 0) {
            Column {
                LinearProgressIndicator(
                    progress = vm.currentProgress,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = vm.currentTestInfo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // نمایش "بهترین نتیجه" به صورت متمایز
        if (vm.scanResults.isNotEmpty()) {
            val best = vm.scanResults.first()
            Text("✅ بهترین تنظیمات پیشنهادی:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            FragmentResultItem(best, isBest = true) {
                clipboardManager.setText(AnnotatedString("${best.length}-${best.interval}"))
                Toast.makeText(context, "کپی شد: ${best.length}-${best.interval}", Toast.LENGTH_SHORT).show()
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        Text("سایر نتایج:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(vm.scanResults.drop(1)) { result ->
                FragmentResultItem(result, isBest = false) {
                    val copyText = "${result.length}-${result.interval}"
                    clipboardManager.setText(AnnotatedString(copyText))
                    Toast.makeText(context, "کپی شد: $copyText", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun FragmentResultItem(result: FragmentResult, isBest: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (isBest) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBest) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        if (result.stability == 100) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("${result.stability}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("${result.length}-${result.interval}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("پایداری در تست", fontSize = 10.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${result.latency}ms", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                Text("تأخیر", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}