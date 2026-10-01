package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.MetricGauge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun SecurityAnalyticsScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val vulnerabilities by viewModel.vulnerabilities.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Security & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Zero-trust posture & global telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // Security Posture Score Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ClaudeSageGreen, modifier = Modifier.size(20.dp))
                            Text("Zero-Trust Certified", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Continuous vulnerability scanning & automated SBOM verification active.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("98", style = MaterialTheme.typography.displayMedium, color = ClaudeSageGreen, fontWeight = FontWeight.Bold)
                        Text("/ 100 Posture", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        // Real-Time Global Performance Telemetry
        item {
            CodeForgeSectionHeader(
                title = "REAL-TIME SERVICE TELEMETRY (GLOBAL)",
                subtitle = "P50, P95, P99 latency percentiles & uptime"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricGauge(label = "P50 Latency", value = "${telemetry.p50LatencyMs}", unit = "ms", accentColor = EmeraldGreen, modifier = Modifier.weight(1f))
                    MetricGauge(label = "P95 Latency", value = "${telemetry.p95LatencyMs}", unit = "ms", accentColor = CyberCyan, modifier = Modifier.weight(1f))
                    MetricGauge(label = "P99 Latency", value = "${telemetry.p99LatencyMs}", unit = "ms", accentColor = NeonPurple, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricGauge(label = "Throughput", value = "38.4k", unit = "req/s", accentColor = ElectricBlue, modifier = Modifier.weight(1f))
                    MetricGauge(label = "Error Rate", value = "${telemetry.errorRatePercent}%", unit = "", accentColor = EmeraldGreen, modifier = Modifier.weight(1f))
                    MetricGauge(label = "Uptime (30d)", value = "${telemetry.uptimePercent}%", unit = "", accentColor = NeonGreen, modifier = Modifier.weight(1f))
                }
            }
        }

        // Custom Canvas Traffic Velocity & Latency Distribution Chart
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRAFFIC & LATENCY VELOCITY (24H)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(text = "LIVE FEED", color = CyberCyan)
                    }

                    // Canvas Bar Chart
                    val barHeights = listOf(0.4f, 0.55f, 0.45f, 0.7f, 0.85f, 0.65f, 0.9f, 0.75f, 0.8f, 0.95f, 0.6f, 0.88f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        val barWidth = (size.width - (barHeights.size - 1) * 8.dp.toPx()) / barHeights.size
                        barHeights.forEachIndexed { i, factor ->
                            val x = i * (barWidth + 8.dp.toPx())
                            val barH = size.height * factor
                            val y = size.height - barH

                            drawRoundRect(
                                color = if (i == barHeights.lastIndex) CyberCyan else NeonPurple.copy(alpha = 0.6f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("00:00 UTC", style = CodeTextStyle.copy(color = TextMuted, fontSize = 9.sp))
                        Text("12:00 UTC", style = CodeTextStyle.copy(color = TextMuted, fontSize = 9.sp))
                        Text("NOW", style = CodeTextStyle.copy(color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Active CVE Vulnerability Scanner Reports
        item {
            CodeForgeSectionHeader(
                title = "VULNERABILITY SCANNER (SAST / DAST)",
                subtitle = "Automated container and dependency vulnerability scan"
            )
        }

        items(vulnerabilities, key = { it.cveId }) { vuln ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("vuln_card_${vuln.cveId}"),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(vuln.severity.colorHex), modifier = Modifier.size(16.dp))
                            Text(vuln.cveId, style = CodeTextStyle.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            Text(vuln.packageName, style = CodeTextStyle.copy(color = TextMuted, fontSize = 11.sp))
                        }
                        StatusBadge(text = vuln.severity.label, color = Color(vuln.severity.colorHex))
                    }

                    Text(vuln.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("Remediation: Upgrade package to ${vuln.fixedInVersion}", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 10.5.sp))
                }
            }
        }

        // Security Audit Log Feed
        item {
            CodeForgeSectionHeader(
                title = "ENTERPRISE ZERO-TRUST AUDIT TRAIL",
                subtitle = "Cryptographically signed mutation logs with IP & MFA verification"
            )
        }

        items(auditLogs, key = { it.id }) { log ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.id}"),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(log.action, style = CodeTextStyle.copy(color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                            if (log.isMfaVerified) {
                                StatusBadge(text = "MFA", color = EmeraldGreen)
                            }
                        }
                        Text("${log.actor} • IP: ${log.ipAddress}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }

                    Text(log.timestamp, style = CodeTextStyle.copy(color = TextMuted, fontSize = 10.sp))
                }
            }
        }
    }
}
