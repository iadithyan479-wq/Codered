package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReplicationState
import com.example.data.model.ServiceHealth
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.MetricGauge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MicroservicesCloudScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val microservices by viewModel.microservices.collectAsState()
    val regions by viewModel.regions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Microservices & Mesh",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Multi-region distributed consensus",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { viewModel.triggerFailoverTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("simulate_failover_btn")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Failover", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Top Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricGauge(
                    label = "Global Ingress",
                    value = "108.5k",
                    unit = "req/s",
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Raft P99 Lag",
                    value = "38",
                    unit = "ms",
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Mesh Nodes",
                    value = "48",
                    unit = "pods",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Multi-Region Persistence Cluster Panel
        item {
            CodeForgeSectionHeader(
                title = "MULTI-REGION ACTIVE-ACTIVE DATA PERSISTENCE",
                subtitle = "Cross-datacenter Raft replication with automatic failover"
            )
        }

        items(regions, key = { it.regionCode }) { region ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("region_card_${region.regionCode}"),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (region.isPrimaryLeader) CyberCyan else DarkBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (region.isPrimaryLeader) CyberCyan else EmeraldGreen)
                            )
                            Column {
                                Text(
                                    text = region.regionName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = region.regionCode,
                                    style = CodeTextStyle.copy(color = TextMuted, fontSize = 10.sp)
                                )
                            }
                        }

                        StatusBadge(
                            text = if (region.isPrimaryLeader) "PRIMARY LEADER" else "ACTIVE REPLICA",
                            color = if (region.isPrimaryLeader) CyberCyan else EmeraldGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Replication Lag", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${region.replicationLagMs} ms", style = CodeTextStyle.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }

                        Column {
                            Text("Storage Allocated", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${region.storageUsageGb} GB", style = CodeTextStyle.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }

                        Column {
                            Text("Failover Readiness", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                if (region.failoverReady) "Hot Standby" else "Cold",
                                style = CodeTextStyle.copy(color = if (region.failoverReady) EmeraldGreen else AmberWarning, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Microservices Mesh Topology
        item {
            CodeForgeSectionHeader(
                title = "MICROSERVICES MESH TOPOLOGY",
                subtitle = "Health metrics and resource distribution per service"
            )
        }

        items(microservices, key = { it.id }) { service ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("service_card_${service.id}"),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = when (service.id) {
                                    "srv-gw" -> Icons.Default.Router
                                    "srv-auth" -> Icons.Default.Security
                                    "srv-collab" -> Icons.Default.GroupWork
                                    "srv-db" -> Icons.Default.Storage
                                    "srv-cache" -> Icons.Default.Memory
                                    else -> Icons.Default.Layers
                                },
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(service.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(service.role, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }

                        StatusBadge(
                            text = service.health.name,
                            color = when (service.health) {
                                ServiceHealth.HEALTHY -> EmeraldGreen
                                ServiceHealth.DEGRADED -> CrimsonError
                                ServiceHealth.SCALING -> AmberWarning
                            }
                        )
                    }

                    // Stats Grid for Service
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU: ${service.cpuPercent}%", style = CodeTextStyle.copy(color = TextSecondary, fontSize = 11.sp))
                        Text("Mem: ${service.memoryMb}MB", style = CodeTextStyle.copy(color = TextSecondary, fontSize = 11.sp))
                        Text("Latency: ${service.latencyMs}ms", style = CodeTextStyle.copy(color = TextCyan, fontSize = 11.sp))
                        Text("Throughput: ${service.requestsPerSec} rps", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 11.sp))
                    }
                }
            }
        }
    }
}
