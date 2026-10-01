package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CodeLanguage
import com.example.ui.components.UserAvatar
import com.example.ui.screens.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeForgeApp(
    viewModel: CodeForgeViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isTerminalDrawerOpen by viewModel.isTerminalDrawerOpen.collectAsState()
    val collaborators by viewModel.collaborators.collectAsState()
    val branches by viewModel.branches.collectAsState()

    val showNewFileDialog by viewModel.showNewFileDialog.collectAsState()
    val showCommitDialog by viewModel.showCommitDialog.collectAsState()
    val showGitHubPushDialog by viewModel.showGitHubPushDialog.collectAsState()
    val isPushingToGitHub by viewModel.isPushingToGitHub.collectAsState()
    val gitHubConfig by viewModel.gitHubConfig.collectAsState()
    val lastGitHubPush by viewModel.lastGitHubPush.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showBranchDropdown by remember { mutableStateOf(false) }

    var newFileName by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(CodeLanguage.TYPESCRIPT) }
    var fastCommitMsg by remember { mutableStateOf("") }

    var ghRepoOwner by remember(gitHubConfig) { mutableStateOf(gitHubConfig.repoOwner) }
    var ghRepoName by remember(gitHubConfig) { mutableStateOf(gitHubConfig.repoName) }
    var ghBranch by remember(gitHubConfig) { mutableStateOf(gitHubConfig.defaultBranch) }
    var ghToken by remember(gitHubConfig) { mutableStateOf(gitHubConfig.personalAccessToken) }
    var ghCommitMsg by remember { mutableStateOf("feat: update workspace via CodeForge") }

    val currentBranch = branches.firstOrNull { it.isCurrent }?.name ?: "main"

    // BackHandler: return to Workspace if in secondary screen
    BackHandler(enabled = currentScreen != ActiveScreen.WORKSPACE || isTerminalDrawerOpen) {
        if (isTerminalDrawerOpen) {
            viewModel.setTerminalDrawer(false)
        } else {
            viewModel.navigateTo(ActiveScreen.WORKSPACE)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Claude Style Brand & Workspace Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Claude-inspired minimal asterisk/sparkle badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClaudeTerracotta.copy(alpha = 0.16f))
                                .border(1.dp, ClaudeTerracotta.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = ClaudeTerracotta,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "CodeForge",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "Workspace",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Branch Switcher Minimal Pill
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { showBranchDropdown = true }
                                .testTag("topbar_branch_selector"),
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Default.AltRoute,
                                    contentDescription = null,
                                    tint = ClaudeCaramel,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = currentBranch,
                                    style = CodeTextStyle.copy(
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showBranchDropdown,
                            onDismissRequest = { showBranchDropdown = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            branches.forEach { b ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (b.isCurrent) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = ClaudeTerracotta,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(b.name, color = TextPrimary, fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.switchBranch(b.name)
                                        showBranchDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Right Section: Network Status, GitHub Push & Collaborators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Online / Offline Toggle Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { viewModel.toggleOfflineMode() }
                                .testTag("topbar_online_offline_pill"),
                            color = if (networkStatus.isOnline) ClaudeSageGreen.copy(alpha = 0.15f) else ClaudeWarmAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (networkStatus.isOnline) ClaudeSageGreen.copy(alpha = 0.4f) else ClaudeWarmAmber.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (networkStatus.isOnline) ClaudeSageGreen else ClaudeWarmAmber)
                                )
                                Text(
                                    text = if (networkStatus.isOnline) "Online" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (networkStatus.isOnline) ClaudeSageGreen else ClaudeWarmAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Direct GitHub Push Action Button
                        IconButton(
                            onClick = { viewModel.setShowGitHubPushDialog(true) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, CircleShape)
                                .testTag("topbar_github_push_btn")
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Push to GitHub",
                                tint = if (gitHubConfig.isConnected) ClaudeTerracotta else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Collaborator Avatars Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-6).dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(ActiveScreen.COLLAB) }
                                .testTag("topbar_collab_avatars")
                        ) {
                            collaborators.take(2).forEach { peer ->
                                UserAvatar(peer.name, peer.avatarColor, sizeDp = 26)
                            }
                            if (collaborators.size > 2) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, DarkBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${collaborators.size - 2}",
                                        style = CodeTextStyle.copy(color = TextSecondary, fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.WORKSPACE,
                        onClick = { viewModel.navigateTo(ActiveScreen.WORKSPACE) },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Editor") },
                        label = { Text("Editor", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClaudeTerracotta,
                            selectedTextColor = ClaudeTerracotta,
                            indicatorColor = ClaudeTerracotta.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_editor")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.TERMINAL,
                        onClick = { viewModel.navigateTo(ActiveScreen.TERMINAL) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal") },
                        label = { Text("Terminal", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClaudeTerracotta,
                            selectedTextColor = ClaudeTerracotta,
                            indicatorColor = ClaudeTerracotta.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_terminal")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.GIT,
                        onClick = { viewModel.navigateTo(ActiveScreen.GIT) },
                        icon = { Icon(Icons.Default.Commit, contentDescription = "Git") },
                        label = { Text("Git", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClaudeTerracotta,
                            selectedTextColor = ClaudeTerracotta,
                            indicatorColor = ClaudeTerracotta.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_git")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.PIPELINES,
                        onClick = { viewModel.navigateTo(ActiveScreen.PIPELINES) },
                        icon = { Icon(Icons.Default.Speed, contentDescription = "CI/CD") },
                        label = { Text("Pipelines", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClaudeTerracotta,
                            selectedTextColor = ClaudeTerracotta,
                            indicatorColor = ClaudeTerracotta.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_pipelines")
                    )

                    NavigationBarItem(
                        selected = showMoreMenuSheet || currentScreen !in listOf(
                            ActiveScreen.WORKSPACE, ActiveScreen.TERMINAL, ActiveScreen.GIT, ActiveScreen.PIPELINES
                        ),
                        onClick = { showMoreMenuSheet = true },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Workspace Tools") },
                        label = { Text("Workspace", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClaudeTerracotta,
                            selectedTextColor = ClaudeTerracotta,
                            indicatorColor = ClaudeTerracotta.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_more_hub")
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen View
            when (currentScreen) {
                ActiveScreen.WORKSPACE -> WorkspaceScreen(viewModel = viewModel)
                ActiveScreen.TERMINAL -> TerminalScreen(viewModel = viewModel)
                ActiveScreen.GIT -> VersionControlScreen(viewModel = viewModel)
                ActiveScreen.CONNECTORS -> ConnectorsScreen(viewModel = viewModel)
                ActiveScreen.COLLAB -> CollaborationScreen(viewModel = viewModel)
                ActiveScreen.PIPELINES -> PipelinesScreen(viewModel = viewModel)
                ActiveScreen.DEPLOYMENTS -> DeploymentPreviewScreen(viewModel = viewModel)
                ActiveScreen.MICROSERVICES -> MicroservicesCloudScreen(viewModel = viewModel)
                ActiveScreen.ISSUES -> IssuesTrackerScreen(viewModel = viewModel)
                ActiveScreen.SECURITY -> SecurityAnalyticsScreen(viewModel = viewModel)
            }

            // Slide-up Terminal Drawer (when toggled from inside Editor)
            AnimatedVisibility(
                visible = isTerminalDrawerOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .align(Alignment.BottomCenter)
            ) {
                TerminalScreen(
                    viewModel = viewModel,
                    isEmbeddedDrawer = true,
                    onCloseDrawer = { viewModel.setTerminalDrawer(false) }
                )
            }
        }
    }

    // DevOps Hub Bottom Sheet (Claude minimalist card style)
    if (showMoreMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenuSheet = false },
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "WORKSPACE TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                val hubItems = listOf(
                    Triple(ActiveScreen.CONNECTORS, "AI Model Connectors", Icons.Default.Extension),
                    Triple(ActiveScreen.COLLAB, "Pair Programming & RBAC", Icons.Default.People),
                    Triple(ActiveScreen.DEPLOYMENTS, "Live Deployment Previews", Icons.Default.Launch),
                    Triple(ActiveScreen.MICROSERVICES, "Microservices & Persistence", Icons.Default.CloudQueue),
                    Triple(ActiveScreen.ISSUES, "Issue Tracking", Icons.Default.BugReport),
                    Triple(ActiveScreen.SECURITY, "Security & Analytics", Icons.Default.Security)
                )

                hubItems.forEach { (screen, title, icon) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.navigateTo(screen)
                                showMoreMenuSheet = false
                            }
                            .testTag("hub_item_${screen.name}"),
                        color = if (currentScreen == screen) ClaudeTerracotta.copy(alpha = 0.12f) else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentScreen == screen) ClaudeTerracotta.copy(alpha = 0.5f) else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (currentScreen == screen) ClaudeTerracotta else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // New File Dialog (Claude minimal dialog)
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowNewFileDialog(false) },
            title = {
                Text(
                    "New File",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("File name", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = { Text("e.g. middleware.ts", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("new_file_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Language", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(CodeLanguage.TYPESCRIPT, CodeLanguage.RUST, CodeLanguage.GO, CodeLanguage.PYTHON).forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang.displayName, fontSize = 10.sp) },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            viewModel.addNewFile(newFileName.trim(), selectedLanguage)
                            newFileName = ""
                            viewModel.setShowNewFileDialog(false)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Create", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowNewFileDialog(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Quick Commit Dialog (Claude minimal dialog)
    if (showCommitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowCommitDialog(false) },
            title = {
                Text(
                    "Commit to $currentBranch",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Commit message", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    OutlinedTextField(
                        value = fastCommitMsg,
                        onValueChange = { fastCommitMsg = it },
                        placeholder = { Text("e.g. feat(collab): add line cursor locks", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("fast_commit_input"),
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
                        if (fastCommitMsg.isNotBlank()) {
                            viewModel.commitChanges(fastCommitMsg.trim())
                            fastCommitMsg = ""
                            viewModel.setShowCommitDialog(false)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Commit & Push", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowCommitDialog(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Direct GitHub Push Dialog (Claude minimal dialog)
    if (showGitHubPushDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowGitHubPushDialog(false) },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ClaudeTerracotta, modifier = Modifier.size(20.dp))
                    Text(
                        "Push to GitHub",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (networkStatus.isOnline) "Direct cloud push to remote GitHub repository" else "Offline mode: commits will be queued locally and pushed upon reconnecting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (networkStatus.isOnline) TextSecondary else ClaudeWarmAmber
                    )

                    OutlinedTextField(
                        value = ghRepoOwner,
                        onValueChange = { ghRepoOwner = it },
                        label = { Text("Owner / Org", fontSize = 11.sp) },
                        placeholder = { Text("e.g. torvalds", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("gh_owner_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = ghRepoName,
                        onValueChange = { ghRepoName = it },
                        label = { Text("Repository Name", fontSize = 11.sp) },
                        placeholder = { Text("e.g. codeforge-app", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("gh_repo_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = ghBranch,
                        onValueChange = { ghBranch = it },
                        label = { Text("Branch", fontSize = 11.sp) },
                        placeholder = { Text("main", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("gh_branch_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = ghCommitMsg,
                        onValueChange = { ghCommitMsg = it },
                        label = { Text("Commit Message", fontSize = 11.sp) },
                        placeholder = { Text("e.g. feat: collaborative IDE update", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("gh_commit_msg_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = ghToken,
                        onValueChange = { ghToken = it },
                        label = { Text("GitHub Token (PAT - Optional)", fontSize = 11.sp) },
                        placeholder = { Text("ghp_... (or leave empty for mock sync)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("gh_token_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClaudeTerracotta,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (lastGitHubPush != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ClaudeSageGreen.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Latest Push Status:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ClaudeSageGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${lastGitHubPush?.message} [${lastGitHubPush?.commitSha}]",
                                    style = CodeTextStyle.copy(color = TextPrimary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateGitHubConfig(
                            gitHubConfig.copy(
                                repoOwner = ghRepoOwner.ifBlank { "enterprise-user" },
                                repoName = ghRepoName.ifBlank { "codeforge-project" },
                                defaultBranch = ghBranch.ifBlank { "main" },
                                personalAccessToken = ghToken
                            )
                        )
                        viewModel.pushDirectlyToGitHub(ghCommitMsg.ifBlank { "Update workspace via CodeForge" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isPushingToGitHub,
                    modifier = Modifier.testTag("gh_push_submit_btn")
                ) {
                    if (isPushingToGitHub) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pushing...", fontSize = 12.sp)
                    } else {
                        Text(if (networkStatus.isOnline) "Push to GitHub" else "Queue Push", fontWeight = FontWeight.Medium)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowGitHubPushDialog(false) }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
