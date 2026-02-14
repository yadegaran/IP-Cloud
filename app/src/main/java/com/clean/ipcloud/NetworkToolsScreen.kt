package com.clean.ipcloud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun NetworkToolsScreen(vm: ScannerViewModel) {
    val scope = rememberCoroutineScope()
    var publicIp by remember { mutableStateOf("در حال دریافت...") }
    var localIp by remember { mutableStateOf(getLocalIpAddress()) }
    var bestMtu by remember { mutableStateOf("-") }
    var currentTestMtu by remember { mutableStateOf(0) }
    var isTestingMtu by remember { mutableStateOf(false) }

    // دریافت اطلاعات اولیه
    LaunchedEffect(Unit) {
        publicIp = getPublicIp()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "جعبه ابزار شبکه",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Text("عیب‌یابی وضعیت اتصال و بهینه‌سازی پارامترها", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // بخش اول: اطلاعات آی‌پی
        ToolCard(title = "اطلاعات آدرس IP") {
            InfoRow("آی‌پی داخلی (LAN):", localIp)
            Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            InfoRow("آی‌پی عمومی (WAN):", publicIp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // بخش دوم: MTU Finder
        ToolCard(title = "یافتن بهترین MTU") {
            Text(
                "اگر فیلترشکن شما وصل می‌شود اما دیتایی رد و بدل نمی‌شود، احتمالاً مقدار MTU شبکه شما محدود شده است.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("مقدار پیشنهادی:", fontSize = 13.sp)
                    Text(
                        if (isTestingMtu) "تست $currentTestMtu..." else bestMtu,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (bestMtu == "-") Color.Gray else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isTestingMtu = true
                    scope.launch {
                        bestMtu = runMtuTest("1.1.1.1") { currentTestMtu = it }
                        isTestingMtu = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isTestingMtu
            ) {
                Text(if (isTestingMtu) "در حال بررسی لایه‌ها..." else "شروع اسکن MTU")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // بخش راهنما
        Text(
            "راهنما: عدد به دست آمده را در بخش MTU تنظیمات فیلترشکن خود وارد کنید (معمولاً بین ۱۲۸۰ تا ۱۴۲۰).",
            fontSize = 11.sp,
            color = Color.Gray,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ToolCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3F51B5))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}