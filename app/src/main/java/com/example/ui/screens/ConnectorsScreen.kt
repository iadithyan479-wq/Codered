package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.model.AIConnector
import com.example.data.model.AIConnectorProvider
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ConnectorsScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val connectors by viewModel.connectors.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedConnectorForEdit by remember { mutableStateOf<AIConnector?>(null) }

    // Add connector state
    var newConnName by remember { mutableStateOf("") }
    var newConnProvider by remember { mutableStateOf(AIConnectorProvider.CLAUDE) }
    var newConnEndpoint by remember { mutableStateOf(AIConnectorProvider.CLAUDE.defaultEndpoint) }
    var newConnApiKey by remember { mutableStateOf("") }
    var newConnModel by remember { mutableStateOf("claude-3-5-sonnet-20241022") }

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
                        text = "Model Connectors",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Plug in Claude, ChatGPT, Grok & local LLMs",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_connector_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Connector", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Active Engine & Offline Banner
        item {
            val active = connectors.firstOrNull { it.isDefault && it.isEnabled }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "PRIMARY WORKSPACE ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = active?.name ?: "DevEngine Neural Core",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (networkStatus.isOnline) "Cloud mesh active • Real-time auto-healing" else "Offline mode active • Local heuristics engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (networkStatus.isOnline) ClaudeSageGreen else ClaudeWarmAmber
                        )
                    }

                    StatusBadge(
                        text = if (networkStatus.isOnline) "ONLINE" else "OFFLINE",
                        color = if (networkStatus.isOnline) ClaudeSageGreen else ClaudeWarmAmber
                    )
                }
            }
        }

        item {
            CodeForgeSectionHeader(
                title = "CONFIGURED CONNECTORS",
                subtitle = "Select default model or configure custom credentials"
            )
        }

        items(connectors, key = { it.id }) { conn ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("connector_card_${conn.id}"),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (conn.isDefault) ClaudeTerracotta.copy(alpha = 0.5f) else DarkBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (conn.provider) {
                                            AIConnectorProvider.CLAUDE -> ClaudeTerracotta.copy(alpha = 0.16f)
                                            AIConnectorProvider.CHATGPT -> ClaudeSageGreen.copy(alpha = 0.16f)
                                            AIConnectorProvider.GROK -> ClaudeCaramel.copy(alpha = 0.16f)
                                            AIConnectorProvider.CUSTOM -> DarkSurfaceVariant
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (conn.provider) {
                                            AIConnectorProvider.CLAUDE -> ClaudeTerracotta.copy(alpha = 0.4f)
                                            AIConnectorProvider.CHATGPT -> ClaudeSageGreen.copy(alpha = 0.4f)
                                            AIConnectorProvider.GROK -> ClaudeCaramel.copy(alpha = 0.4f)
                                            AIConnectorProvider.CUSTOM -> DarkBorder
                                        },
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (conn.provider) {
                                        AIConnectorProvider.CLAUDE -> "C"
                                        AIConnectorProvider.CHATGPT -> "G"
                                        AIConnectorProvider.GROK -> "X"
                                        AIConnectorProvider.CUSTOM -> "L"
                                    },
                                    color = when (conn.provider) {
                                        AIConnectorProvider.CLAUDE -> ClaudeTerracotta
                                        AIConnectorProvider.CHATGPT -> ClaudeSageGreen
                                        AIConnectorProvider.GROK -> ClaudeCaramel
                                        AIConnectorProvider.CUSTOM -> TextPrimary
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Column {
                                Text(
                                    text = conn.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${conn.modelName} • ${conn.latencyMs}ms",
                                    style = CodeTextStyle.copy(color = TextMuted, fontSize = 11.sp)
                                )
                            }
                        }

                        Switch(
                            checked = conn.isEnabled,
                            onCheckedChange = { viewModel.toggleConnector(conn.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = ClaudeTerracotta,
                                uncheckedTrackColor = DarkSurfaceVariant,
                                uncheckedThumbColor = TextMuted
                            )
                        )
                    }

                    // Action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (conn.isDefault) {
                            StatusBadge(text = "Active Workspace Engine", color = ClaudeTerracotta)
                        } else {
                            TextButton(
                                onClick = { viewModel.setDefaultConnector(conn.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Set as Default", color = ClaudeTerracotta, fontSize = 11.5.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { selectedConnectorForEdit = conn },
                            shape = RoundedCornerShape(100.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(if (conn.apiKey.isBlank()) "Set API Key" else "Configured", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Add Connector Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Model Connector", color = TextPrimary, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provider", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AIConnectorProvider.values().forEach { prov ->
                            FilterChip(
                                selected = newConnProvider == prov,
                                onClick = {
                                    newConnProvider = prov
                                    newConnEndpoint = prov.defaultEndpoint
                                    newConnModel = when (prov) {
                                        AIConnectorProvider.CLAUDE -> "claude-3-5-sonnet-20241022"
                                        AIConnectorProvider.CHATGPT -> "gpt-4o"
                                        AIConnectorProvider.GROK -> "grok-2-latest"
                                        AIConnectorProvider.CUSTOM -> "deepseek-coder:6.7b"
                                    }
                                },
                                label = { Text(prov.displayName.split(" ")[0], fontSize = 10.sp) },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ClaudeTerracotta.copy(alpha = 0.2f),
                                    selectedLabelColor = ClaudeTerracotta,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Text("Connector Name", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = newConnName,
                        onValueChange = { newConnName = it },
                        placeholder = { Text("e.g. My Anthropic Claude Team", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("API Key / Bearer Token", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = newConnApiKey,
                        onValueChange = { newConnApiKey = it },
                        placeholder = { Text("sk-ant-... or sk-...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Model Identifier", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = newConnModel,
                        onValueChange = { newConnModel = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newConnName.ifBlank { newConnProvider.displayName }
                        viewModel.addConnector(
                            AIConnector(
                                id = "conn-${java.util.UUID.randomUUID().toString().take(6)}",
                                name = name,
                                provider = newConnProvider,
                                endpointUrl = newConnEndpoint,
                                apiKey = newConnApiKey,
                                modelName = newConnModel,
                                isEnabled = true,
                                isDefault = false
                            )
                        )
                        newConnName = ""
                        newConnApiKey = ""
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Connector")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit API Key Dialog
    if (selectedConnectorForEdit != null) {
        val conn = selectedConnectorForEdit!!
        var apiKeyEdit by remember { mutableStateOf(conn.apiKey) }
        var modelEdit by remember { mutableStateOf(conn.modelName) }

        AlertDialog(
            onDismissRequest = { selectedConnectorForEdit = null },
            title = { Text("Configure ${conn.name}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("API Key", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = apiKeyEdit,
                        onValueChange = { apiKeyEdit = it },
                        placeholder = { Text("Enter API Key securely...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Model Name", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = modelEdit,
                        onValueChange = { modelEdit = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateConnectorKey(conn.id, apiKeyEdit, modelEdit)
                        selectedConnectorForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedConnectorForEdit = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
