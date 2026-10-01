package com.example.data.model

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Files & Code Editor Models
// ==========================================

enum class CodeLanguage(val extension: String, val displayName: String) {
    KOTLIN("kt", "Kotlin"),
    TYPESCRIPT("ts", "TypeScript"),
    PYTHON("py", "Python"),
    RUST("rs", "Rust"),
    GO("go", "Go"),
    DOCKERFILE("dockerfile", "Dockerfile"),
    YAML("yml", "YAML / CI"),
    JSON("json", "JSON")
}

data class ProjectFile(
    val id: String,
    val name: String,
    val path: String,
    val language: CodeLanguage,
    val content: String,
    val isModified: Boolean = false,
    val lockedBy: String? = null // collaborator name if locked
)

data class CodeComment(
    val id: String,
    val authorName: String,
    val authorAvatarColor: Long,
    val lineNumber: Int,
    val comment: String,
    val timestamp: String
)

// ==========================================
// 2. Collaborative Pair Programming & RBAC
// ==========================================

enum class UserRole(val title: String, val level: Int) {
    OWNER("Owner", 5),
    ADMIN("Admin", 4),
    MAINTAINER("Maintainer", 3),
    CONTRIBUTOR("Contributor", 2),
    VIEWER("Viewer", 1)
}

data class Collaborator(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarColor: Long,
    val activeFilePath: String,
    val cursorLine: Int,
    val isTyping: Boolean = false,
    val isInVoiceChannel: Boolean = false,
    val statusText: String = "Online"
)

data class RolePermissionSetting(
    val role: UserRole,
    val canPushDirectlyToMain: Boolean,
    val canTriggerProdDeployment: Boolean,
    val canAccessSecrets: Boolean,
    val canManageTeam: Boolean,
    val canMergePullRequests: Boolean
)

// ==========================================
// 3. Git Version Control Models
// ==========================================

data class GitBranch(
    val name: String,
    val isDefault: Boolean = false,
    val isCurrent: Boolean = false,
    val aheadCount: Int = 0,
    val behindCount: Int = 0
)

data class GitDiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNum: Int? = null,
    val newLineNum: Int? = null
)

enum class DiffLineType {
    CONTEXT, ADDITION, DELETION, HEADER
}

data class GitCommit(
    val hash: String,
    val shortHash: String = hash.take(7),
    val message: String,
    val author: String,
    val authorColor: Long,
    val relativeTime: String,
    val branch: String,
    val additions: Int,
    val deletions: Int,
    val filesChanged: List<String>
)

data class StagedFile(
    val path: String,
    val isStaged: Boolean,
    val additions: Int,
    val deletions: Int,
    val status: String // "modified", "untracked", "deleted"
)

// ==========================================
// 4. Interactive Terminal Models
// ==========================================

enum class TerminalLineType {
    INPUT,
    STDOUT,
    STDERR,
    SUCCESS,
    INFO,
    SYSTEM
}

data class TerminalLine(
    val id: String,
    val text: String,
    val type: TerminalLineType = TerminalLineType.STDOUT,
    val timestamp: String = ""
)

// ==========================================
// 5. Automated CI/CD Pipelines
// ==========================================

enum class PipelineStatus {
    RUNNING, SUCCESS, FAILED, QUEUED
}

data class PipelineStage(
    val name: String,
    val status: PipelineStatus,
    val durationSeconds: Int,
    val logs: List<String>
)

data class PipelineRun(
    val id: String,
    val branch: String,
    val commitHash: String,
    val commitMessage: String,
    val triggeredBy: String,
    val status: PipelineStatus,
    val startedTimeAgo: String,
    val totalDurationSeconds: Int,
    val stages: List<PipelineStage>
)

// ==========================================
// 6. Interactive Deployment Previews
// ==========================================

enum class PreviewDevice(val label: String, val widthDp: Int, val heightDp: Int) {
    MOBILE("Mobile", 360, 640),
    TABLET("Tablet", 540, 720),
    DESKTOP("Full Width", 0, 0)
}

data class DeploymentEnvironment(
    val name: String, // "Development", "Staging", "Production"
    val url: String,
    val port: Int,
    val activeCommit: String,
    val status: String, // "Live", "Deploying", "Healthy"
    val sslActive: Boolean = true,
    val lastDeployedAgo: String,
    val activeReplicas: Int
)

// ==========================================
// 7. Bug & Issue Tracking (DevCycle)
// ==========================================

enum class IssueSeverity(val label: String, val colorHex: Long) {
    CRITICAL("Critical", 0xFFEF4444),
    HIGH("High", 0xFFF97316),
    MEDIUM("Medium", 0xFFF59E0B),
    LOW("Low", 0xFF10B981)
}

enum class IssueStatus(val label: String) {
    BACKLOG("Backlog"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    RESOLVED("Resolved")
}

data class IssueTicket(
    val id: String,
    val title: String,
    val description: String,
    val severity: IssueSeverity,
    val status: IssueStatus,
    val assigneeName: String,
    val assigneeColor: Long,
    val linkedBranch: String? = null,
    val linkedCommit: String? = null,
    val tags: List<String> = emptyList(),
    val createdAgo: String = "2h ago"
)

// ==========================================
// 8. Microservices & Multi-Region Persistence
// ==========================================

enum class ServiceHealth {
    HEALTHY, DEGRADED, SCALING
}

data class MicroserviceNode(
    val id: String,
    val name: String,
    val role: String, // "API Gateway", "Auth Service", "Collab Sync Engine", "Postgres Primary", "Redis Cache"
    val health: ServiceHealth,
    val cpuPercent: Int,
    val memoryMb: Int,
    val latencyMs: Int,
    val requestsPerSec: Int,
    val version: String
)

enum class ReplicationState {
    SYNCHRONIZED, REPLICATING, DEGRADED
}

data class PersistenceRegion(
    val regionCode: String, // "us-east-1", "eu-central-1", "ap-southeast-1"
    val regionName: String,
    val isPrimaryLeader: Boolean,
    val status: ReplicationState,
    val replicationLagMs: Int,
    val storageUsageGb: Double,
    val failoverReady: Boolean
)

// ==========================================
// 9. Enterprise Security & Audit
// ==========================================

data class SecurityAuditEntry(
    val id: String,
    val timestamp: String,
    val actor: String,
    val action: String,
    val ipAddress: String,
    val status: String, // "ALLOWED", "BLOCKED", "VERIFIED"
    val isMfaVerified: Boolean
)

data class VulnerabilityAlert(
    val cveId: String,
    val packageName: String,
    val severity: IssueSeverity,
    val fixedInVersion: String,
    val description: String
)

// ==========================================
// 10. Analytics & Telemetry Snapshot
// ==========================================

data class EnvironmentTelemetry(
    val environment: String,
    val p50LatencyMs: Int,
    val p95LatencyMs: Int,
    val p99LatencyMs: Int,
    val throughputRps: Int,
    val errorRatePercent: Double,
    val uptimePercent: Double,
    val activeCollaborators: Int
)

// ==========================================
// 11. Model Connectors (Claude, ChatGPT, Grok, etc.)
// ==========================================

enum class AIConnectorProvider(val displayName: String, val defaultEndpoint: String) {
    CLAUDE("Claude (Anthropic)", "https://api.anthropic.com/v1/messages"),
    CHATGPT("ChatGPT (OpenAI)", "https://api.openai.com/v1/chat/completions"),
    GROK("Grok (xAI)", "https://api.x.ai/v1/chat/completions"),
    CUSTOM("Custom / Ollama / Local", "http://localhost:11434/api/generate")
}

data class AIConnector(
    val id: String,
    val name: String,
    val provider: AIConnectorProvider,
    val endpointUrl: String,
    val apiKey: String,
    val modelName: String,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val latencyMs: Int = 42,
    val lastTestedStatus: String = "Ready"
)

// ==========================================
// 12. Direct GitHub Integration
// ==========================================

data class GitHubConfig(
    val personalAccessToken: String = "",
    val repoOwner: String = "enterprise-user",
    val repoName: String = "codeforge-project",
    val defaultBranch: String = "main",
    val isConnected: Boolean = true,
    val lastPushedCommitSha: String? = null,
    val lastPushedAt: String? = null
)

data class GitHubPushResult(
    val success: Boolean,
    val commitSha: String,
    val branch: String,
    val message: String,
    val filesPushedCount: Int,
    val repoUrl: String,
    val timestamp: String
)

// ==========================================
// 13. Auto-Correction Engine
// ==========================================

data class AutoCorrectionResult(
    val fileId: String,
    val originalCode: String,
    val correctedCode: String,
    val explanation: String,
    val changesCount: Int,
    val appliedTimestamp: String,
    val engineName: String = "DevEngine Auto-Correction",
    val isOfflineCorrection: Boolean = false
)

// ==========================================
// 14. Network Status (Online & Offline)
// ==========================================

data class NetworkStatus(
    val isOnline: Boolean = true,
    val isManuallyOffline: Boolean = false,
    val activeConnectionType: String = "Wi-Fi Cloud Mesh",
    val pendingOfflineCommitsCount: Int = 0
)

