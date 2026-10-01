package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.DeploymentEnvironment
import com.example.data.model.PreviewDevice
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DeploymentPreviewScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val environments by viewModel.environments.collectAsState()
    val activeEnv by viewModel.activePreviewEnv.collectAsState()

    var selectedDevice by remember { mutableStateOf(PreviewDevice.MOBILE) }
    var currentPath by remember { mutableStateOf("/") }
    var interactiveCounter by remember { mutableIntStateOf(42) }
    var livePingMs by remember { mutableIntStateOf(14) }
    var showConsoleLogs by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Environment Selector Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEPLOYMENT PREVIEW SANDBOX",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(text = activeEnv.status, color = EmeraldGreen)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(environments) { env ->
                        val isSelected = env.name == activeEnv.name
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectPreviewEnvironment(env) }
                                .testTag("env_tab_${env.name}"),
                            color = if (isSelected) CyberCyan.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyberCyan else DarkBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyberCyan else TextMuted)
                                )
                                Text(
                                    text = env.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = ":${env.port}",
                                    style = CodeTextStyle.copy(color = TextCyan, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Browser Address Bar & Device Viewport Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "SSL Secure", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${activeEnv.url}$currentPath",
                        style = CodeTextStyle.copy(color = TextPrimary, fontSize = 11.sp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            livePingMs = (8..24).random()
                        },
                        modifier = Modifier.size(28.dp).testTag("preview_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                }

                // Viewport & Route Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Viewport size toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PreviewDevice.values().forEach { device ->
                            FilterChip(
                                selected = selectedDevice == device,
                                onClick = { selectedDevice = device },
                                label = { Text(device.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan,
                                    selectedLabelColor = DarkBackground
                                ),
                                modifier = Modifier.testTag("device_chip_${device.name}")
                            )
                        }
                    }

                    // Console logs toggle
                    FilledTonalButton(
                        onClick = { showConsoleLogs = !showConsoleLogs },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant, contentColor = TextCyan),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (showConsoleLogs) "Hide Console" else "Console (OK)", fontSize = 10.sp)
                    }
                }
            }
        }

        // Live Simulated Application Sandbox
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mock App Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(CyberCyan), contentAlignment = Alignment.Center) {
                                Text("CF", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("CodeForge Service", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }

                        StatusBadge(text = "${livePingMs}ms ping", color = NeonGreen)
                    }
                }

                // Interactive Sandbox Counter Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interactive Live Sandbox Component",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Text(
                            text = "$interactiveCounter",
                            style = MaterialTheme.typography.displayLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Real-time state mutations synchronized across all clients",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { interactiveCounter++ },
                                colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.testTag("sandbox_increment_btn")
                            ) {
                                Text("+ Increment", fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = { interactiveCounter = 0 },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text("Reset", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Sandbox Routes Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("/" to "Root", "/api/health" to "Health", "/metrics" to "Prometheus").forEach { (path, label) ->
                        OutlinedButton(
                            onClick = { currentPath = path },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (currentPath == path) CyberCyan else DarkBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (currentPath == path) CyberCyan else TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Expandable Browser Network & Console Logs Drawer
        if (showConsoleLogs) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(8.dp),
                color = TerminalBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DEVTOOLS NETWORK & CONSOLE (HTTP/2 200 OK)",
                        style = CodeTextStyle.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "[WS] Connected to wss://preview.codeforge.internal/live-sync (14ms)",
                        style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 10.sp)
                    )
                    Text(
                        text = "[GET] ${activeEnv.url}$currentPath -> 200 OK (size: 34.2kB)",
                        style = CodeTextStyle.copy(color = TextCyan, fontSize = 10.sp)
                    )
                    Text(
                        text = "[STATE] Synced CRDT operation from peer Sarah Chen",
                        style = CodeTextStyle.copy(color = TextSecondary, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}
