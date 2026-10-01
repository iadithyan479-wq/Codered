package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.PipelineRun
import com.example.data.model.PipelineStatus
import com.example.ui.CodeForgeViewModel
import com.example.ui.components.CodeForgeSectionHeader
import com.example.ui.components.MetricGauge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PipelinesScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val pipelines by viewModel.pipelines.collectAsState()
    var expandedRunId by remember { mutableStateOf<String?>(pipelines.firstOrNull()?.id) }

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
                        text = "Automated CI/CD",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Build, security verification & deployment",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { viewModel.triggerPipeline() },
                    colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta, contentColor = TextPrimary),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("trigger_pipeline_run_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Run", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Pipeline Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricGauge(
                    label = "Success Rate",
                    value = "99.4%",
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Avg Duration",
                    value = "1m 42s",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricGauge(
                    label = "Security Gate",
                    value = "0 CVE",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CodeForgeSectionHeader(
                title = "PIPELINE EXECUTION RUNS",
                subtitle = "Real-time workflow matrices and stage streaming"
            )
        }

        items(pipelines, key = { it.id }) { run ->
            val isExpanded = expandedRunId == run.id

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expandedRunId = if (isExpanded) null else run.id }
                    .testTag("pipeline_card_${run.id}"),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (run.status == PipelineStatus.RUNNING) CyberCyan.copy(alpha = 0.6f) else DarkBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Run Header
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
                                    .background(
                                        when (run.status) {
                                            PipelineStatus.SUCCESS -> EmeraldGreen
                                            PipelineStatus.RUNNING -> CyberCyan
                                            PipelineStatus.FAILED -> CrimsonError
                                            PipelineStatus.QUEUED -> AmberWarning
                                        }
                                    )
                            )
                            Text(
                                text = run.id,
                                style = CodeTextStyle.copy(color = CyberCyan, fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = run.branch,
                                    style = CodeTextStyle.copy(color = TextSecondary, fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        StatusBadge(
                            text = run.status.name,
                            color = when (run.status) {
                                PipelineStatus.SUCCESS -> EmeraldGreen
                                PipelineStatus.RUNNING -> CyberCyan
                                PipelineStatus.FAILED -> CrimsonError
                                PipelineStatus.QUEUED -> AmberWarning
                            }
                        )
                    }

                    Text(
                        text = run.commitMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Triggered by ${run.triggeredBy} • ${run.startedTimeAgo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "Total: ${run.totalDurationSeconds}s",
                            style = CodeTextStyle.copy(color = TextCyan, fontSize = 11.sp)
                        )
                    }

                    // Visual Horizontal Stages Matrix
                    val stageScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(stageScrollState)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        run.stages.forEachIndexed { idx, stage ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (stage.status) {
                                    PipelineStatus.SUCCESS -> EmeraldGreen.copy(alpha = 0.15f)
                                    PipelineStatus.RUNNING -> CyberCyan.copy(alpha = 0.15f)
                                    PipelineStatus.FAILED -> CrimsonError.copy(alpha = 0.15f)
                                    PipelineStatus.QUEUED -> DarkSurfaceVariant
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (stage.status) {
                                        PipelineStatus.SUCCESS -> EmeraldGreen.copy(alpha = 0.5f)
                                        PipelineStatus.RUNNING -> CyberCyan
                                        PipelineStatus.FAILED -> CrimsonError
                                        PipelineStatus.QUEUED -> DarkBorder
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (stage.status) {
                                            PipelineStatus.SUCCESS -> Icons.Default.Check
                                            PipelineStatus.RUNNING -> Icons.Default.Autorenew
                                            PipelineStatus.FAILED -> Icons.Default.Close
                                            PipelineStatus.QUEUED -> Icons.Default.Schedule
                                        },
                                        contentDescription = null,
                                        tint = when (stage.status) {
                                            PipelineStatus.SUCCESS -> EmeraldGreen
                                            PipelineStatus.RUNNING -> CyberCyan
                                            PipelineStatus.FAILED -> CrimsonError
                                            PipelineStatus.QUEUED -> TextMuted
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stage.name.split(" ").take(2).joinToString(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (idx < run.stages.size - 1) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Expandable Live Logs
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "STAGE EXECUTION LOGS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )

                            run.stages.forEach { stage ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "> ${stage.name}",
                                            style = CodeTextStyle.copy(color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        )
                                        Text(
                                            text = "${stage.durationSeconds}s",
                                            style = CodeTextStyle.copy(color = TextMuted, fontSize = 10.sp)
                                        )
                                    }

                                    stage.logs.forEach { log ->
                                        Text(
                                            text = "  $log",
                                            style = CodeTextStyle.copy(color = TextSecondary, fontSize = 10.5.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
