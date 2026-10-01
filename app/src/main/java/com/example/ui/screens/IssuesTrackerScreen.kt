package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.IssueSeverity
import com.example.data.model.IssueStatus
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun IssuesTrackerScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val issues by viewModel.issues.collectAsState()
    var selectedFilter by remember { mutableStateOf<IssueStatus?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var issueTitle by remember { mutableStateOf("") }
    var issueDesc by remember { mutableStateOf("") }
    var issueSeverity by remember { mutableStateOf(IssueSeverity.HIGH) }

    val filteredIssues = remember(issues, selectedFilter) {
        if (selectedFilter == null) issues else issues.filter { it.status == selectedFilter }
    }

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
                        text = "Issue Tracking",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Linked to commits & CI workflows",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_issue_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Issue", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Status Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${issues.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
                items(IssueStatus.values()) { status ->
                    val count = issues.count { it.status == status }
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = status },
                        label = { Text("${status.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
            }
        }

        items(filteredIssues, key = { it.id }) { issue ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("issue_card_${issue.id}"),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Issue Title & Severity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = issue.id,
                                    style = CodeTextStyle.copy(color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = issue.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        StatusBadge(
                            text = issue.severity.label,
                            color = Color(issue.severity.colorHex)
                        )
                    }

                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Linked metadata row (Branch, Commit, Assignee)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            UserAvatar(issue.assigneeName, issue.assigneeColor, sizeDp = 22)
                            Text(issue.assigneeName, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text("•", color = TextMuted)
                            Text(issue.createdAgo, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }

                        if (issue.linkedCommit != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Commit, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(12.dp))
                                    Text(issue.linkedCommit, style = CodeTextStyle.copy(color = NeonPurple, fontSize = 10.sp))
                                }
                            }
                        }
                    }

                    // Move Status Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Status:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        IssueStatus.values().forEach { st ->
                            val isCurrent = issue.status == st
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updateIssueStatus(issue.id, st) },
                                color = if (isCurrent) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan) else null
                            ) {
                                Text(
                                    text = st.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) CyberCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Issue Modal Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("File New Bug / Issue Ticket", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Title:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = issueTitle,
                        onValueChange = { issueTitle = it },
                        placeholder = { Text("e.g. WebSocket connection drop on VPN") },
                        modifier = Modifier.fillMaxWidth().testTag("new_issue_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Description & Reproduction Steps:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = issueDesc,
                        onValueChange = { issueDesc = it },
                        placeholder = { Text("Reproduction steps, stacktrace...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("new_issue_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Severity:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IssueSeverity.values().forEach { sev ->
                            FilterChip(
                                selected = issueSeverity == sev,
                                onClick = { issueSeverity = sev },
                                label = { Text(sev.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(sev.colorHex),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (issueTitle.isNotBlank()) {
                            viewModel.createIssue(issueTitle, issueDesc, issueSeverity)
                            issueTitle = ""
                            issueDesc = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
                ) {
                    Text("Create Issue Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
