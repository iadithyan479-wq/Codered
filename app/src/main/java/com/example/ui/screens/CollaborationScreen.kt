package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.UserRole
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun CollaborationScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val collaborators by viewModel.collaborators.collectAsState()
    val isVoiceActive by viewModel.isVoiceActive.collectAsState()
    val comments by viewModel.comments.collectAsState()

    var showInviteModal by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    var selectedInviteRole by remember { mutableStateOf(UserRole.CONTRIBUTOR) }
    var expandedUserRoleDropdown by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Header & Live Voice Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    .background(NeonGreen)
                            )
                            Column {
                                Text(
                                    text = "Live Pair Programming Mesh",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Real-time CRDT sync with WebSockets & Raft consensus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { showInviteModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("invite_collab_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Voice Channel Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isVoiceActive) NeonPurple.copy(alpha = 0.15f) else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isVoiceActive) NeonPurple else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (isVoiceActive) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (isVoiceActive) NeonPurple else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isVoiceActive) "Voice Room: Connected (4 Active)" else "Voice Room: Disconnected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isVoiceActive) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isVoiceActive) "Spatial audio • Noise suppression active" else "Tap to join pair discussion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { viewModel.toggleVoiceChannel() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isVoiceActive) CrimsonError.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f),
                                    contentColor = if (isVoiceActive) CrimsonError else CyberCyan
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("toggle_voice_btn")
                            ) {
                                Text(if (isVoiceActive) "Leave Audio" else "Join Audio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Collaborators List & Role Editor
        item {
            CodeForgeSectionHeader(
                title = "ACTIVE COLLABORATORS & PRESENCE",
                subtitle = "Fine-grained presence and live cursor locks"
            )
        }

        items(collaborators, key = { it.id }) { user ->
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("user_collab_card_${user.id}"),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(name = user.name, colorHex = user.avatarColor, sizeDp = 40)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(user.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                                if (user.isTyping) {
                                    Text("● typing", style = MaterialTheme.typography.labelSmall, color = NeonGreen, fontSize = 10.sp)
                                }
                            }
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "${user.activeFilePath} : ${user.cursorLine}",
                                    style = CodeTextStyle.copy(color = CyberCyan, fontSize = 10.5.sp)
                                )
                            }
                        }
                    }

                    // Role Badge & Dropdown
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    expandedUserRoleDropdown = if (expandedUserRoleDropdown == user.id) null else user.id
                                }
                                .testTag("role_badge_${user.id}"),
                            color = when (user.role) {
                                UserRole.OWNER -> NeonPurple.copy(alpha = 0.2f)
                                UserRole.ADMIN -> ElectricBlue.copy(alpha = 0.2f)
                                UserRole.MAINTAINER -> EmeraldGreen.copy(alpha = 0.2f)
                                UserRole.CONTRIBUTOR -> AmberWarning.copy(alpha = 0.2f)
                                UserRole.VIEWER -> TextMuted.copy(alpha = 0.2f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (user.role) {
                                    UserRole.OWNER -> NeonPurple
                                    UserRole.ADMIN -> ElectricBlue
                                    UserRole.MAINTAINER -> EmeraldGreen
                                    UserRole.CONTRIBUTOR -> AmberWarning
                                    UserRole.VIEWER -> TextMuted
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = user.role.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = (expandedUserRoleDropdown == user.id),
                            onDismissRequest = { expandedUserRoleDropdown = null },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.title, color = TextPrimary) },
                                    onClick = {
                                        viewModel.updateCollaboratorRole(user.id, role)
                                        expandedUserRoleDropdown = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Granular RBAC Permissions Matrix
        item {
            CodeForgeSectionHeader(
                title = "ROLE-BASED ACCESS CONTROL (RBAC) MATRIX",
                subtitle = "Enterprise security enforcement & branch protection rules"
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val permissions = listOf(
                        "Push to Main (Branch Protection)" to "Admin / Maintainer only",
                        "Trigger Production Multi-Region Deployment" to "Admin only (requires MFA)",
                        "Read/Write Cloud Vault Secrets" to "Owner & Admin only",
                        "Invite & Manage Team Roles" to "Admin & Owner only",
                        "Approve Pull Request Merges" to "Maintainer & Admin"
                    )

                    permissions.forEach { (perm, roleAllowed) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(perm, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(roleAllowed, style = MaterialTheme.typography.labelSmall, color = TextCyan)
                            }
                            Icon(Icons.Default.Lock, contentDescription = "Protected", tint = NeonPurple, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Real-Time Code Comments & Discussions Feed
        item {
            CodeForgeSectionHeader(
                title = "LIVE CODE COMMENTS & THREADS",
                subtitle = "Inline feedback across active files"
            )
        }

        items(comments, key = { it.id }) { comment ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                            UserAvatar(comment.authorName, comment.authorAvatarColor, sizeDp = 22)
                            Text(comment.authorName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            StatusBadge(text = "Line ${comment.lineNumber}", color = CyberCyan)
                        }
                        Text(comment.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }

                    Text(
                        text = comment.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // Invite Modal Dialog
    if (showInviteModal) {
        AlertDialog(
            onDismissRequest = { showInviteModal = false },
            title = { Text("Invite Developer to Workspace", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Developer Email:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        placeholder = { Text("dev@enterprise.io") },
                        modifier = Modifier.fillMaxWidth().testTag("invite_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Assign Role:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(UserRole.CONTRIBUTOR, UserRole.MAINTAINER, UserRole.ADMIN).forEach { role ->
                            FilterChip(
                                selected = selectedInviteRole == role,
                                onClick = { selectedInviteRole = role },
                                label = { Text(role.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan,
                                    selectedLabelColor = DarkBackground
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteEmail.isNotBlank()) {
                            showInviteModal = false
                            inviteEmail = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
                ) {
                    Text("Send Workspace Invite")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
