package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitCommit
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun VersionControlScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val branches by viewModel.branches.collectAsState()
    val commits by viewModel.commits.collectAsState()
    val stagedFiles by viewModel.stagedFiles.collectAsState()
    val gitHubConfig by viewModel.gitHubConfig.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    var showNewBranchDialog by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }
    var commitMessageInput by remember { mutableStateOf("") }
    var selectedCommitForDiff by remember { mutableStateOf<GitCommit?>(null) }

    val currentBranch = branches.firstOrNull { it.isCurrent }?.name ?: "main"

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
                        text = "Version Control",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "github.com/${gitHubConfig.repoOwner}/${gitHubConfig.repoName} • ${if (networkStatus.isOnline) "Synced" else "Local Offline"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (networkStatus.isOnline) TextMuted else ClaudeWarmAmber
                    )
                }

                Button(
                    onClick = {
                        viewModel.setShowGitHubPushDialog(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("git_push_remote_btn")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Push GitHub", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Branch Switcher Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = ClaudeCaramel, modifier = Modifier.size(16.dp))
                            Text(
                                text = "BRANCHES",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }

                        TextButton(
                            onClick = { showNewBranchDialog = true },
                            modifier = Modifier.testTag("create_branch_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp), tint = ClaudeTerracotta)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Branch", color = ClaudeTerracotta, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(branches) { b ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { viewModel.switchBranch(b.name) }
                                    .testTag("branch_chip_${b.name}"),
                                color = if (b.isCurrent) ClaudeTerracotta.copy(alpha = 0.16f) else DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (b.isCurrent) ClaudeTerracotta.copy(alpha = 0.6f) else DarkBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (b.isCurrent) ClaudeTerracotta else TextMuted)
                                    )
                                    Text(
                                        text = b.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (b.isCurrent) ClaudeTerracotta else TextSecondary,
                                        fontWeight = if (b.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    if (b.aheadCount > 0) {
                                        Text(
                                            text = "↑${b.aheadCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ClaudeSageGreen,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Staging & Commit Composer
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
                            text = "STAGING AREA (${stagedFiles.size} changed files)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(text = "Branch: $currentBranch", color = CyberCyan)
                    }

                    // Staged files list
                    stagedFiles.forEach { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (file.isStaged) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (file.isStaged) EmeraldGreen else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = file.path,
                                    style = CodeTextStyle.copy(fontSize = 12.sp, color = TextPrimary)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("+${file.additions}", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 11.sp))
                                Text("-${file.deletions}", style = CodeTextStyle.copy(color = CrimsonError, fontSize = 11.sp))
                            }
                        }
                    }

                    // Commit Message Input
                    OutlinedTextField(
                        value = commitMessageInput,
                        onValueChange = { commitMessageInput = it },
                        placeholder = { Text("feat(mesh): add multi-region failover handler", style = CodeTextStyle.copy(color = TextMuted)) },
                        modifier = Modifier.fillMaxWidth().testTag("commit_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (commitMessageInput.isNotBlank()) {
                                viewModel.commitChanges(commitMessageInput)
                                commitMessageInput = ""
                            }
                        },
                        enabled = commitMessageInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_commit_btn"),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Commit & Run Pipeline", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Visual Commit History / Graph
        item {
            Text(
                text = "COMMIT GRAPH & AUDIT HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        items(commits, key = { it.hash }) { commit ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedCommitForDiff = commit }
                    .testTag("commit_card_${commit.shortHash}"),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Commit tree node
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                                .border(2.dp, DarkSurface, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(50.dp)
                                .background(DarkBorder)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = commit.message,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text(
                                    text = commit.shortHash,
                                    style = CodeTextStyle.copy(color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserAvatar(commit.author, commit.authorColor, sizeDp = 20)
                            Text(commit.author, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("•", color = TextMuted)
                            Text(commit.relativeTime, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("•", color = TextMuted)
                            Text("+${commit.additions}", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 11.sp))
                            Text("-${commit.deletions}", style = CodeTextStyle.copy(color = CrimsonError, fontSize = 11.sp))
                        }

                        // Affected files chip row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(commit.filesChanged) { file ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DarkBackground
                                ) {
                                    Text(
                                        text = file,
                                        style = CodeTextStyle.copy(color = TextMuted, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Branch Dialog
    if (showNewBranchDialog) {
        AlertDialog(
            onDismissRequest = { showNewBranchDialog = false },
            title = { Text("Create New Git Branch", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Branch will branch off of '$currentBranch'", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = newBranchName,
                        onValueChange = { newBranchName = it },
                        placeholder = { Text("e.g. feature/crdt-sync") },
                        modifier = Modifier.fillMaxWidth().testTag("new_branch_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
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
                        if (newBranchName.isNotBlank()) {
                            viewModel.createBranch(newBranchName.trim())
                            newBranchName = ""
                            showNewBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
                ) {
                    Text("Create Branch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBranchDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Selected Commit Diff Dialog
    if (selectedCommitForDiff != null) {
        val commit = selectedCommitForDiff!!
        AlertDialog(
            onDismissRequest = { selectedCommitForDiff = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Commit Diff [${commit.shortHash}]", color = TextPrimary, fontSize = 16.sp)
                    StatusBadge(text = "+${commit.additions} -${commit.deletions}", color = EmeraldGreen)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(commit.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Authored by ${commit.author} • ${commit.relativeTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SyntaxBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("@@ -12,4 +12,8 @@ WebSocketServer", style = CodeTextStyle.copy(color = TextMuted, fontSize = 10.sp))
                            Text("+ activePeers.set(user.id, ws);", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 11.sp))
                            Text("+ broadcastPresence({ type: 'PEER_JOIN', user });", style = CodeTextStyle.copy(color = EmeraldGreen, fontSize = 11.sp))
                            Text("- legacySocketPool.add(ws);", style = CodeTextStyle.copy(color = CrimsonError, fontSize = 11.sp))
                            Text("  ws.on('message', (payload) => {", style = CodeTextStyle.copy(color = TextCode, fontSize = 11.sp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedCommitForDiff = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
                ) {
                    Text("Close")
                }
            },
            containerColor = DarkSurface
        )
    }
}
