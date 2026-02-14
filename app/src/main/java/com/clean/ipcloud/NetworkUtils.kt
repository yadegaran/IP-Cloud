package com.clean.ipcloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.URL
import java.util.Collections

object NetworkUtils {


    suspend fun fetchCloudflareInfo(ip: String): Pair<String, String> =
        withContext(Dispatchers.IO) {
            try {
                // استفاده از آدرس مستقیم کلاودفلر برای تست سریع
                val url = URL("http://$ip/cdn-cgi/trace")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 1000
                conn.readTimeout = 1000
                conn.setRequestProperty("Host", "browserleaks.com") // ترفند برای عبور از بلاک

                val text = conn.inputStream.bufferedReader().readText()
                val colo =
                    text.lineSequence().firstOrNull { it.startsWith("colo=") }?.split("=")?.get(1)
                        ?: "N/A"
                val loc =
                    text.lineSequence().firstOrNull { it.startsWith("loc=") }?.split("=")?.get(1)
                        ?: "??"

                Pair(colo, loc)
            } catch (e: Exception) {
                Pair("Timeout", "??")
            }
        }

    suspend fun checkDataExchange(ip: String, port: Int): String = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 1500)
            socket.soTimeout = 1500
            val output = socket.getOutputStream()
            val input = socket.getInputStream()

            // ارسال یک درخواست بسیار سبک
            output.write("GET /cdn-cgi/trace HTTP/1.1\r\nHost: cloudflare.com\r\n\r\n".toByteArray())

            val buffer = ByteArray(1024)
            val bytesRead = input.read(buffer)

            socket.close()
            if (bytesRead > 0) "تبادل موفق" else "بدون پاسخ"
        } catch (e: Exception) {
            "خطای تبادل"
        }
    }
}


// دریافت آی‌پی داخلی گوشی
fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in Collections.list(interfaces)) {
            val addrs = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr.hostAddress.contains(".")) {
                    return addr.hostAddress
                }
            }
        }
    } catch (ex: Exception) {
    }
    return "نامشخص"
}

// دریافت آی‌پی عمومی (از طریق سرویس icanhazip)
suspend fun getPublicIp(): String {
    return withContext(Dispatchers.IO) {
        try {
            URL("https://icanhazip.com").readText().trim()
        } catch (e: Exception) {
            "عدم اتصال"
        }
    }
}

// تست هوشمند MTU با استفاده از دستور پینگ سیستم
suspend fun runMtuTest(target: String, onStep: (Int) -> Unit): String {
    return withContext(Dispatchers.IO) {
        var resultMtu = 1500
        // تست از ۱۵۰۰ به پایین با گام‌های ۱۰تایی
        for (mtu in 1500 downTo 1200 step 10) {
            onStep(mtu)
            try {
                // -s سایز پکت، -c تعداد، -W زمان انتظار، -M do جلوگیری از تکه شدن
                val process = Runtime.getRuntime().exec("ping -c 1 -s $mtu -M do $target")
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    resultMtu = mtu + 28 // اضافه کردن هدر IP/ICMP
                    break
                }
            } catch (e: Exception) {
            }
        }
        resultMtu.toString()
    }
}