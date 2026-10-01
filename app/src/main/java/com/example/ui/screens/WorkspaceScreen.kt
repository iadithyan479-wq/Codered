package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeLanguage
import com.example.data.model.ProjectFile
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val activeFile by viewModel.activeFile.collectAsState()
    val files by viewModel.files.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val collaborators by viewModel.collaborators.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val autoCorrectionResult by viewModel.autoCorrectionResult.collectAsState()
    val isAutoCorrecting by viewModel.isAutoCorrecting.collectAsState()

    var showFileExplorerDrawer by remember { mutableStateOf(false) }
    var showCommentBox by remember { mutableStateOf(false) }
    var commentLineNum by remember { mutableStateOf(1) }
    var commentText by remember { mutableStateOf("") }
    var codeSearchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    val fileContent = activeFile.content
    val lines = remember(fileContent) { fileContent.split("\n") }

    // Collaborators editing the current active file
    val activePeersOnFile = remember(collaborators, activeFile) {
        collaborators.filter { it.activeFilePath == activeFile.path }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Workspace Breadcrumb & Tab Bar
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column {
                    // Top File Tabs & Drawer Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showFileExplorerDrawer = !showFileExplorerDrawer },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("file_explorer_toggle")
                        ) {
                            Icon(
                                imageVector = if (showFileExplorerDrawer) Icons.Default.Close else Icons.Default.Folder,
                                contentDescription = "Toggle Files",
                                tint = CyberCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Scrollable file tabs
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(openTabs) { tab ->
                                val isSelected = tab.id == activeFile.id
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.selectFile(tab) }
                                        .testTag("tab_${tab.name}"),
                                    color = if (isSelected) SyntaxBackground else DarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) CyberCyan.copy(alpha = 0.5f) else DarkBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = when (tab.language) {
                                                CodeLanguage.TYPESCRIPT -> "TS"
                                                CodeLanguage.RUST -> "RS"
                                                CodeLanguage.PYTHON -> "PY"
                                                CodeLanguage.GO -> "GO"
                                                CodeLanguage.YAML -> "YM"
                                                CodeLanguage.DOCKERFILE -> "DK"
                                                else -> "KT"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (tab.language) {
                                                CodeLanguage.TYPESCRIPT -> ElectricBlue
                                                CodeLanguage.RUST -> NeonPurple
                                                CodeLanguage.PYTHON -> EmeraldGreen
                                                CodeLanguage.GO -> CyberCyan
                                                else -> AmberWarning
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )

                                        Text(
                                            text = tab.name + if (tab.isModified) " *" else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )

                                        if (openTabs.size > 1) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close Tab",
                                                tint = TextMuted,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { viewModel.closeTab(tab) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Online / Offline Status Toggle Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { viewModel.toggleOfflineMode() }
                                .testTag("online_offline_toggle"),
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

                        Spacer(modifier = Modifier.width(4.dp))

                        // Search and New File Buttons
                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            modifier = Modifier.size(32.dp).testTag("search_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (showSearchBar) CyberCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setShowNewFileDialog(true) },
                            modifier = Modifier.size(32.dp).testTag("new_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New File",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Optional Search Bar
                    if (showSearchBar) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            color = DarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = codeSearchQuery,
                                    onValueChange = { codeSearchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = CodeTextStyle.copy(fontSize = 12.sp, color = TextPrimary),
                                    cursorBrush = SolidColor(CyberCyan),
                                    decorationBox = { innerTextField ->
                                        if (codeSearchQuery.isEmpty()) {
                                            Text("Find in file...", style = CodeTextStyle.copy(fontSize = 12.sp, color = TextMuted))
                                        }
                                        innerTextField()
                                    }
                                )
                                if (codeSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { codeSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Presence banner showing active collaborators in this file
                    if (activePeersOnFile.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Text(
                                text = "Pairing now:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            activePeersOnFile.forEach { peer ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(peer.avatarColor).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    UserAvatar(peer.name, peer.avatarColor, sizeDp = 16)
                                    Text(
                                        text = "${peer.name.split(" ")[0]} (L${peer.cursorLine})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(peer.avatarColor),
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (peer.isTyping) {
                                        Text("typing...", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main Editor Canvas (Gutter with Line Numbers + Interactive Code Area)
            val editorScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(SyntaxBackground)
            ) {
                // Line Number Gutter
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(EditorGutter)
                        .verticalScroll(editorScrollState)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { index, _ ->
                        val lineNum = index + 1
                        val peerAtLine = activePeersOnFile.firstOrNull { it.cursorLine == lineNum }
                        val hasComment = comments.any { it.lineNumber == lineNum }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clickable {
                                    commentLineNum = lineNum
                                    showCommentBox = true
                                }
                                .padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (peerAtLine != null) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(peerAtLine.avatarColor))
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            } else if (hasComment) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarning)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = "$lineNum",
                                style = CodeTextStyle.copy(
                                    color = if (peerAtLine != null) Color(peerAtLine.avatarColor) else TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Code Content Editor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(editorScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    val styledCode = remember(activeFile.content, codeSearchQuery) {
                        highlightSyntax(activeFile.content, activeFile.language, codeSearchQuery)
                    }

                    BasicTextField(
                        value = activeFile.content,
                        onValueChange = { newText ->
                            viewModel.updateCode(newText)
                        },
                        textStyle = CodeTextStyle.copy(fontSize = 12.5.sp, lineHeight = 22.sp),
                        cursorBrush = SolidColor(CyberCyan),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_field"),
                        visualTransformation = {
                            androidx.compose.ui.text.input.TransformedText(
                                styledCode,
                                androidx.compose.ui.text.input.OffsetMapping.Identity
                            )
                        }
                    )
                }
            }

            // Auto-Correction Analysis & Proposal Card (Claude minimal artifact style)
            AnimatedVisibility(
                visible = autoCorrectionResult != null || isAutoCorrecting,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ClaudeTerracotta.copy(alpha = 0.5f)),
                    shadowElevation = 6.dp
                ) {
                    if (isAutoCorrecting) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ClaudeTerracotta
                            )
                            Text(
                                text = "DevEngine intelligent auto-correction analyzing syntax & logic...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    } else {
                        autoCorrectionResult?.let { result ->
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("✦", color = ClaudeTerracotta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Auto-Correction Proposed",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    StatusBadge(
                                        text = if (result.isOfflineCorrection) "OFFLINE HEURISTIC" else "SMART REPAIR",
                                        color = if (result.isOfflineCorrection) ClaudeWarmAmber else ClaudeSageGreen
                                    )
                                }

                                Text(
                                    text = result.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { viewModel.dismissAutoCorrection() },
                                        modifier = Modifier.testTag("dismiss_auto_correction_btn")
                                    ) {
                                        Text("Dismiss", color = TextMuted, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.applyAutoCorrection() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ClaudeTerracotta,
                                            contentColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(100.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("apply_auto_correction_btn")
                                    ) {
                                        Text("Accept & Apply Fix", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Quick Action Bar
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Auto-Fix / Auto-Correct Button
                        Button(
                            onClick = { viewModel.autoCorrectActiveFile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClaudeTerracotta.copy(alpha = 0.2f),
                                contentColor = ClaudeTerracotta
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ClaudeTerracotta.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("auto_correct_button"),
                            enabled = !isAutoCorrecting
                        ) {
                            Text("✦", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ClaudeTerracotta)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Fix", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        // Quick run/test
                        Button(
                            onClick = {
                                viewModel.executeTerminalCommand("npm test")
                                viewModel.setTerminalDrawer(true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClaudeTerracotta,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("run_tests_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Tests", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        // Push to GitHub button
                        OutlinedButton(
                            onClick = { viewModel.setShowGitHubPushDialog(true) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("quick_github_push_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp), tint = ClaudeSageGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GitHub", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        // Quick Commit
                        OutlinedButton(
                            onClick = { viewModel.setShowCommitDialog(true) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("quick_commit_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = ClaudeTerracotta)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Commit", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Terminal Drawer Button
                    FilledTonalButton(
                        onClick = { viewModel.toggleTerminalDrawer() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(100.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("toggle_terminal_chip")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = ClaudeTerracotta)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terminal", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Left File Explorer Drawer Overlay
        if (showFileExplorerDrawer) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.CenterStart),
                color = DarkSurface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROJECT FILES",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showFileExplorerDrawer = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(files) { file ->
                            val isSelected = file.id == activeFile.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.selectFile(file)
                                        showFileExplorerDrawer = false
                                    }
                                    .testTag("file_item_${file.name}"),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.15f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isSelected) CyberCyan else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) CyberCyan else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = file.path,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                    if (file.isModified) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AmberWarning)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            showFileExplorerDrawer = false
                            viewModel.setShowNewFileDialog(true)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("drawer_add_file_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New File", fontSize = 12.sp)
                    }
                }
            }
        }

        // Add Line Comment Dialog
        if (showCommentBox) {
            AlertDialog(
                onDismissRequest = { showCommentBox = false },
                title = { Text("Add Code Comment (Line $commentLineNum)", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Collaborative note for peers:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("e.g., Needs null-check before serialization") },
                            modifier = Modifier.fillMaxWidth().testTag("comment_input_field"),
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
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(commentLineNum, commentText)
                                commentText = ""
                                showCommentBox = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
                    ) {
                        Text("Post Comment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCommentBox = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

// Visual Syntax Highlighter helper
fun highlightSyntax(code: String, language: CodeLanguage, searchQuery: String): AnnotatedString {
    return buildAnnotatedString {
        append(code)

        val keywords = listOf(
            "const", "let", "var", "function", "return", "async", "await", "import", "export",
            "from", "class", "extends", "pub", "fn", "struct", "impl", "use", "match", "mut",
            "package", "def", "if", "else", "for", "while", "type", "val", "fun", "override",
            "true", "false", "null", "nil", "apiVersion", "kind", "metadata", "spec", "containers",
            "FROM", "WORKDIR", "COPY", "RUN", "CMD", "EXPOSE", "USER"
        )

        val types = listOf(
            "WebSocketServer", "WebSocket", "Claims", "Result", "String", "Vec", "Map",
            "MultiRegionDBCluster", "Deployment", "MetricsCollector", "Error", "Int", "Boolean"
        )

        // Highlight Keywords
        keywords.forEach { kw ->
            val regex = "\\b$kw\\b".toRegex()
            regex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Highlight Types
        types.forEach { t ->
            val regex = "\\b$t\\b".toRegex()
            regex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = SyntaxType, fontWeight = FontWeight.Medium),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Highlight String Literals
        val stringRegex = "(\"[^\"]*\"|'[^']*'|`[^`]*`)".toRegex()
        stringRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxString),
                match.range.first,
                match.range.last + 1
            )
        }

        // Highlight Comments
        val commentRegex = "(//[^\n]*|#[^\n]*)".toRegex()
        commentRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxComment),
                match.range.first,
                match.range.last + 1
            )
        }

        // Highlight search query if active
        if (searchQuery.isNotEmpty()) {
            val searchRegex = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE)
            searchRegex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(background = AmberWarning.copy(alpha = 0.5f), color = Color.White),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }
}
