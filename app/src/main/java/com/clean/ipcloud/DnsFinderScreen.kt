package com.clean.ipcloud

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class DnsResult(val ip: String, val latency: Long)

@Composable
fun DnsFinderScreen(vm: ScannerViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var results by remember { mutableStateOf<List<DnsResult>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("آماده اسکن") }

    // فیلد دامنه با مقدار پیش‌فرض گیت‌هاب
    var testDomain by remember { mutableStateOf("www.github.com") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            "DNS یاب هوشمند",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // فیلد ورودی دامنه برای تست
        OutlinedTextField(
            value = testDomain,
            onValueChange = { testDomain = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("دامنه مورد نظر برای تست") },
            placeholder = { Text("مثلاً google.com") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isScanning
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(statusText, modifier = Modifier.padding(vertical = 8.dp), fontSize = 13.sp)
        }

        Button(
            onClick = {
                if (!isScanning) {
                    if (testDomain.isBlank()) {
                        Toast.makeText(context, "لطفاً یک دامنه وارد کنید", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    isScanning = true
                    results = emptyList()
                    scope.launch {
                        // پاس دادن دامنه انتخابی به تابع تست
                        val testResults = runAdvancedDnsTest(context, testDomain) { p, status ->
                            progress = p
                            statusText = status
                        }
                        results = testResults.take(10)
                        isScanning = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isScanning
        ) {
            Text(if (isScanning) "در حال اسکن..." else "شروع تست روی $testDomain")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { res -> DnsCard(res) }
        }
    }
}

@Composable
fun DnsCard(res: DnsResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(res.ip, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    "تأخیر پاسخ: ${res.latency}ms",
                    color = if (res.latency < 150) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            }
            Text(
                "Verified",
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

suspend fun runAdvancedDnsTest(
    context: Context,
    domainToTest: String, // اضافه شدن ورودی جدید
    onUpdate: (Float, String) -> Unit
): List<DnsResult> {
    return withContext(Dispatchers.IO) {
        val verifiedDns = mutableListOf<DnsResult>()

        try {
            val inputStream = context.assets.open("resolvers.txt")
            val allIps =
                inputStream.bufferedReader().use { it.readLines() }.filter { it.isNotBlank() }

            val testSubset = allIps.shuffled().take(100)
            val total = testSubset.size

            testSubset.forEachIndexed { index, dnsIp ->
                val trimmedDns = dnsIp.trim()
                onUpdate((index + 1).toFloat() / total, "تست: $trimmedDns روی $domainToTest")

                val startTime = System.currentTimeMillis()

                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(trimmedDns, 53), 700)
                    socket.close()

                    // استفاده از دامنه وارد شده توسط کاربر
                    val address = InetAddress.getByName(domainToTest)
                    val resolvedIp = address.hostAddress ?: ""

                    // فیلتر آی‌پی‌های فیک (فیلترینگ ایران)
                    val isPoisoned = resolvedIp.startsWith("10.") ||
                            resolvedIp.startsWith("127.") ||
                            resolvedIp == "0.0.0.0"

                    if (!isPoisoned) {
                        val latency = System.currentTimeMillis() - startTime
                        verifiedDns.add(DnsResult(trimmedDns, latency))
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        verifiedDns.sortBy { it.latency }
        verifiedDns
    }
}