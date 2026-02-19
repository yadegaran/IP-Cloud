package com.tools.net

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// مدل داده ورژن



class UpdateManager {
    object updateManager {
        private const val UPDATE_URL =
            "https://raw.githubusercontent.com/yadegaran/Tools-Networrk/refs/heads/master/update.json"

        // دریافت اطلاعات آپدیت از سرور
        suspend fun fetchUpdateInfo(): UpdateInfo? {
            return withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                try {
                    val request = Request.Builder().url(UPDATE_URL).build()
                    val response = client.newCall(request).execute()
                    val jsonData = response.body?.string() ?: return@withContext null
                    val obj = JSONObject(jsonData)
                    UpdateInfo(
                        versionCode = obj.getInt("versionCode"),
                        downloadUrl = obj.getString("downloadUrl"),
                        mirrorUrl = obj.getString("mirrorUrl"),
                        changeLog = obj.getString("changeLog")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        // گرفتن نسخه عددی فعلی اپلیکیشن
        fun getCurrentVersionCode(context: Context): Int {
            return try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        0
                    ).longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                }
            } catch (e: Exception) {
                1
            }
        }

        // گرفتن نام نسخه (مثلاً 1.0.6)
        fun getAppVersionName(context: Context): String {
            return try {
                val packageInfo =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                packageInfo.versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }
        }

        // مدیریت دانلود
        fun startDownload(context: Context, url: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, "در حال انتقال به مرورگر...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در باز کردن مرورگر!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}