package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TerminalLineType
import com.example.ui.CodeForgeViewModel
import com.example.ui.theme.*

@Composable
fun TerminalScreen(
    viewModel: CodeForgeViewModel,
    isEmbeddedDrawer: Boolean = false,
    onCloseDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new logs arrive
    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    val quickCommands = listOf(
        "git status",
        "npm test",
        "cargo test",
        "docker ps",
        "kubectl get pods",
        "failover",
        "ls",
        "whoami",
        "help",
        "clear"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        // Claude-Style Quiet Terminal Header
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ClaudeTerracotta)
                    )

                    Text(
                        text = "terminal / workspace",
                        style = TerminalTextStyle.copy(
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("clear") },
                        modifier = Modifier.size(28.dp).testTag("terminal_clear_btn")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isEmbeddedDrawer && onCloseDrawer != null) {
                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier.size(28.dp).testTag("terminal_close_drawer")
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize",
                                tint = ClaudeTerracotta,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Claude Minimal Quick Command Pills
        Surface(
            color = DarkSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(quickCommands) { cmd ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable {
                                viewModel.executeTerminalCommand(cmd)
                            }
                            .testTag("quick_cmd_$cmd"),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = cmd,
                            style = TerminalTextStyle.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Terminal Log Console Output (Claude Artifact style)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(terminalLogs, key = { it.id }) { log ->
                val textColor = when (log.type) {
                    TerminalLineType.INPUT -> ClaudeTerracotta
                    TerminalLineType.SUCCESS -> ClaudeSageGreen
                    TerminalLineType.STDERR -> ClaudeWarmRed
                    TerminalLineType.SYSTEM -> ClaudeCaramel
                    TerminalLineType.INFO -> TextSecondary
                    TerminalLineType.STDOUT -> TextCode
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    if (log.timestamp.isNotEmpty()) {
                        Text(
                            text = "${log.timestamp}  ",
                            style = TerminalTextStyle.copy(color = TextMuted, fontSize = 10.sp),
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    Text(
                        text = log.text,
                        style = TerminalTextStyle.copy(color = textColor, fontSize = 12.sp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Claude Prompt Input Bar (Clean floating pill container)
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "› ",
                    style = TerminalTextStyle.copy(
                        color = ClaudeTerracotta,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                BasicTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    textStyle = TerminalTextStyle.copy(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(ClaudeTerracotta),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (currentInput.isNotBlank()) {
                                viewModel.executeTerminalCommand(currentInput)
                                currentInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field"),
                    decorationBox = { innerTextField ->
                        if (currentInput.isEmpty()) {
                            Text(
                                text = "Run a command or test...",
                                style = TerminalTextStyle.copy(color = TextMuted, fontSize = 12.5.sp)
                            )
                        }
                        innerTextField()
                    }
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (currentInput.isNotBlank()) ClaudeTerracotta else DarkSurfaceVariant)
                        .clickable {
                            if (currentInput.isNotBlank()) {
                                viewModel.executeTerminalCommand(currentInput)
                                currentInput = ""
                            }
                        }
                        .testTag("terminal_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Execute",
                        tint = if (currentInput.isNotBlank()) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
