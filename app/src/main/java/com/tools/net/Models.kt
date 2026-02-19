package com.tools.net


data class IpScanResult(
    val ip: String,
    val port: Int,
    val latency: Long,
    val isSuccess: Boolean,
    var colo: String = "جستجو...",
    var countryCode: String = "??",
    var exchangeStatus: String = "در حال بررسی...",
    var mtu: Int = 1420,
    var packetLoss: Int = 0
)


data class UpdateInfo(
    val versionCode: Int,
    val downloadUrl: String,
    val mirrorUrl: String,
    val changeLog: String
)


data class FragmentResult(
    val length: Int,
    val interval: Int,
    val latency: Long,
    val stability: Int
)

data class AnalysisStep(val title: String, val status: AnalysisStatus, val message: String)
enum class AnalysisStatus { LOADING, SUCCESS, ERROR, WARNING }

data class FAQItem(val question: String, val answer: String)