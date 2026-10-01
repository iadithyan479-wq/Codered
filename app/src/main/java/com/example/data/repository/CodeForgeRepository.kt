package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CodeForgeRepository(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    // ==========================================
    // 1. Files & Active Document State
    // ==========================================
    private val initialFiles = listOf(
        ProjectFile(
            id = "file-1",
            name = "server.ts",
            path = "src/server.ts",
            language = CodeLanguage.TYPESCRIPT,
            content = """// CodeForge Real-time Collab Server
import { WebSocketServer, WebSocket } from 'ws';
import { verifyEnterpriseToken } from './auth/jwt';
import { MetricsCollector } from './telemetry';

const wss = new WebSocketServer({ port: 8080 });
const activePeers = new Map<string, WebSocket>();

wss.on('connection', async (ws, req) => {
  const token = req.headers['authorization']?.split(' ')[1];
  const user = await verifyEnterpriseToken(token);
  
  if (!user || user.role === 'BANNED') {
    ws.close(4003, 'Unauthorized RBAC Policy Violation');
    return;
  }

  activePeers.set(user.id, ws);
  broadcastPresence({ type: 'PEER_JOIN', user, region: 'us-east-1' });

  ws.on('message', (payload: string) => {
    const event = JSON.parse(payload);
    // Operational Transformation (OT) / CRDT for co-editing
    handleCRDTOperation(user.id, event);
    MetricsCollector.recordLatency(event.timestamp);
  });

  ws.on('close', () => {
    activePeers.delete(user.id);
    broadcastPresence({ type: 'PEER_LEAVE', userId: user.id });
  });
});

console.log('🚀 CodeForge Collab Engine listening on port :8080');
""".trimIndent()
        ),
        ProjectFile(
            id = "file-2",
            name = "jwt.rs",
            path = "src/auth/jwt.rs",
            language = CodeLanguage.RUST,
            content = """// High Performance Zero-Trust Token Verification
use jsonwebtoken::{decode, DecodingKey, Validation, Algorithm};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct Claims {
    pub sub: String,
    pub company_id: String,
    pub role: String,
    pub exp: usize,
    pub allowed_regions: Vec<String>,
}

pub fn verify_enterprise_token(token: &str, secret: &[u8]) -> Result<Claims, String> {
    let mut validation = Validation::new(Algorithm::HS256);
    validation.validate_exp = true;
    
    match decode::<Claims>(token, &DecodingKey::from_secret(secret), &validation) {
        Ok(token_data) => {
            // Enterprise RBAC check
            if token_data.claims.role == "Admin" || token_data.claims.role == "Maintainer" {
                Ok(token_data.claims)
            } else {
                Err("RBAC: Insufficient privilege".into())
            }
        },
        Err(err) => Err(format!("Token decode failure: {:?}", err)),
    }
}
""".trimIndent()
        ),
        ProjectFile(
            id = "file-3",
            name = "deployment.yml",
            path = "k8s/deployment.yml",
            language = CodeLanguage.YAML,
            content = """apiVersion: apps/v1
kind: Deployment
metadata:
  name: codeforge-core-mesh
  namespace: production
  labels:
    tier: enterprise-backend
spec:
  replicas: 6
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2
      maxUnavailable: 0
  selector:
    matchLabels:
      app: codeforge-engine
  template:
    metadata:
      labels:
        app: codeforge-engine
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                topologyKey: topology.kubernetes.io/zone
      containers:
      - name: codeforge-app
        image: gcr.io/codeforge-cloud/engine:v2.4.1
        ports:
        - containerPort: 8080
        resources:
          limits:
            cpu: "2000m"
            memory: "4096Mi"
          requests:
            cpu: "500m"
            memory: "1024Mi"
        env:
        - name: PERSISTENCE_REGION
          value: "multi-region-active-active"
""".trimIndent()
        ),
        ProjectFile(
            id = "file-4",
            name = "persistence.go",
            path = "services/persistence.go",
            language = CodeLanguage.GO,
            content = """package main

import (
	"context"
	"fmt"
	"time"
)

type MultiRegionDBCluster struct {
	PrimaryRegion string
	SyncInterval  time.Duration
	ReplicaLagMs  int64
}

func (c *MultiRegionDBCluster) SyncCrossRegion(ctx context.Context, key string, data []byte) error {
	startTime := time.Now()
	// Raft Consensus across US-East, EU-Central, AP-South
	fmt.Printf("[Raft] Broadcasting consensus entry for key: %s\n", key)
	c.ReplicaLagMs = time.Since(startTime).Milliseconds()
	return nil
}
""".trimIndent()
        ),
        ProjectFile(
            id = "file-5",
            name = "test_collab.py",
            path = "tests/test_collab.py",
            language = CodeLanguage.PYTHON,
            content = """import pytest
import time

def test_concurrent_cursor_transforms():
    initial_buffer = "function compute() { return 42; }"
    user_a_op = ("insert", 18, "/* async */ ")
    user_b_op = ("insert", 28, " * 2")
    
    # Simulate Operational Transformation
    transformed_buffer = "function compute/* async */ () { return 42 * 2; }"
    assert len(transformed_buffer) > len(initial_buffer)
    print("✓ Concurrent OT resolution verified with zero conflict")

def test_rbac_token_rejection():
    invalid_token = "expired.jwt.token"
    assert "expired" in invalid_token
""".trimIndent()
        ),
        ProjectFile(
            id = "file-6",
            name = "Dockerfile",
            path = "Dockerfile",
            language = CodeLanguage.DOCKERFILE,
            content = """FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM gcr.io/distroless/nodejs20-debian12
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
USER nonroot:nonroot
EXPOSE 8080
CMD ["dist/server.js"]
""".trimIndent()
        )
    )

    private val _files = MutableStateFlow(initialFiles)
    val files: StateFlow<List<ProjectFile>> = _files.asStateFlow()

    private val _activeFile = MutableStateFlow(initialFiles[0])
    val activeFile: StateFlow<ProjectFile> = _activeFile.asStateFlow()

    private val _openTabs = MutableStateFlow(listOf(initialFiles[0], initialFiles[1]))
    val openTabs: StateFlow<List<ProjectFile>> = _openTabs.asStateFlow()

    // ==========================================
    // 2. Real-time Pair Programming & Presence
    // ==========================================
    private val initialCollaborators = listOf(
        Collaborator(
            id = "user-sarah",
            name = "Sarah Chen",
            email = "sarah.c@codeforge.io",
            role = UserRole.MAINTAINER,
            avatarColor = 0xFF00E5FF,
            activeFilePath = "src/server.ts",
            cursorLine = 12,
            isTyping = true,
            isInVoiceChannel = true,
            statusText = "Co-authoring OT Transform"
        ),
        Collaborator(
            id = "user-alex",
            name = "Alex Rivera",
            email = "alex.r@codeforge.io",
            role = UserRole.ADMIN,
            avatarColor = 0xFFA855F7,
            activeFilePath = "k8s/deployment.yml",
            cursorLine = 22,
            isTyping = false,
            isInVoiceChannel = true,
            statusText = "Reviewing K8s Pod anti-affinity"
        ),
        Collaborator(
            id = "user-elena",
            name = "Elena Rostov",
            email = "elena.r@codeforge.io",
            role = UserRole.CONTRIBUTOR,
            avatarColor = 0xFF10B981,
            activeFilePath = "services/persistence.go",
            cursorLine = 16,
            isTyping = false,
            isInVoiceChannel = false,
            statusText = "Benchmarking Raft Sync"
        ),
        Collaborator(
            id = "user-marcus",
            name = "Marcus Vance",
            email = "marcus.v@codeforge.io",
            role = UserRole.VIEWER,
            avatarColor = 0xFFF59E0B,
            activeFilePath = "tests/test_collab.py",
            cursorLine = 6,
            isTyping = false,
            isInVoiceChannel = false,
            statusText = "Auditing CI results"
        )
    )

    private val _collaborators = MutableStateFlow(initialCollaborators)
    val collaborators: StateFlow<List<Collaborator>> = _collaborators.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(true)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _comments = MutableStateFlow(
        listOf(
            CodeComment("c1", "Sarah Chen", 0xFF00E5FF, 14, "Ensure we add rate-limiting per client IP here before prod release.", "10m ago"),
            CodeComment("c2", "Alex Rivera", 0xFFA855F7, 24, "Verified that MetricsCollector flushes to OpenTelemetry exporter properly.", "4m ago")
        )
    )
    val comments: StateFlow<List<CodeComment>> = _comments.asStateFlow()

    // ==========================================
    // 3. Git Version Control State
    // ==========================================
    private val initialBranches = listOf(
        GitBranch("main", isDefault = true, isCurrent = true, aheadCount = 2, behindCount = 0),
        GitBranch("feature/realtime-collab", isDefault = false, isCurrent = false, aheadCount = 4, behindCount = 1),
        GitBranch("hotfix/jwt-auth", isDefault = false, isCurrent = false, aheadCount = 1, behindCount = 0),
        GitBranch("release/v2.4.0", isDefault = false, isCurrent = false, aheadCount = 0, behindCount = 0)
    )

    private val _branches = MutableStateFlow(initialBranches)
    val branches: StateFlow<List<GitBranch>> = _branches.asStateFlow()

    private val initialCommits = listOf(
        GitCommit(
            hash = "7f8b92c4e1a",
            message = "feat(collab): add high-throughput WebSocket presence broadcast",
            author = "Sarah Chen",
            authorColor = 0xFF00E5FF,
            relativeTime = "25m ago",
            branch = "main",
            additions = 84,
            deletions = 12,
            filesChanged = listOf("src/server.ts", "src/auth/jwt.rs")
        ),
        GitCommit(
            hash = "3e1a90fd42b",
            message = "ci(pipeline): add zero-trust SAST scanner & multi-region deploy",
            author = "Alex Rivera",
            authorColor = 0xFFA855F7,
            relativeTime = "1h ago",
            branch = "main",
            additions = 142,
            deletions = 35,
            filesChanged = listOf("k8s/deployment.yml", "Dockerfile")
        ),
        GitCommit(
            hash = "9c21ef88ab3",
            message = "perf(persistence): implement Raft consensus cross-region replication",
            author = "Elena Rostov",
            authorColor = 0xFF10B981,
            relativeTime = "3h ago",
            branch = "main",
            additions = 210,
            deletions = 48,
            filesChanged = listOf("services/persistence.go", "tests/test_collab.py")
        )
    )

    private val _commits = MutableStateFlow(initialCommits)
    val commits: StateFlow<List<GitCommit>> = _commits.asStateFlow()

    private val _stagedFiles = MutableStateFlow(
        listOf(
            StagedFile("src/server.ts", isStaged = true, additions = 14, deletions = 3, status = "modified"),
            StagedFile("services/persistence.go", isStaged = false, additions = 22, deletions = 7, status = "modified"),
            StagedFile("docs/architecture.md", isStaged = false, additions = 45, deletions = 0, status = "untracked")
        )
    )
    val stagedFiles: StateFlow<List<StagedFile>> = _stagedFiles.asStateFlow()

    // ==========================================
    // 4. Interactive Embedded Terminal State
    // ==========================================
    private val initialTerminalLogs = listOf(
        TerminalLine("t-0", "CodeForge Studio v2.4.1 (Linux 6.8.0-cloud-x86_64)", TerminalLineType.SYSTEM, "19:20:00"),
        TerminalLine("t-1", "Workspace initialized in /workspace/codeforge-enterprise", TerminalLineType.INFO, "19:20:01"),
        TerminalLine("t-2", "Connected to multi-region mesh: us-east-1 (leader), eu-central-1, ap-southeast-1", TerminalLineType.SUCCESS, "19:20:02"),
        TerminalLine("t-3", "$ git status", TerminalLineType.INPUT, "19:21:10"),
        TerminalLine("t-4", "On branch main. Your branch is ahead of 'origin/main' by 2 commits.\nChanges to be committed: (use \"git restore --staged <file>...\" to unstage)\n  modified: src/server.ts", TerminalLineType.STDOUT, "19:21:10")
    )

    private val _terminalLogs = MutableStateFlow(initialTerminalLogs)
    val terminalLogs: StateFlow<List<TerminalLine>> = _terminalLogs.asStateFlow()

    // ==========================================
    // 5. Automated CI/CD Pipelines
    // ==========================================
    private val initialPipelines = listOf(
        PipelineRun(
            id = "pipe-9082",
            branch = "main",
            commitHash = "7f8b92c",
            commitMessage = "feat(collab): add high-throughput WebSocket presence broadcast",
            triggeredBy = "Sarah Chen",
            status = PipelineStatus.SUCCESS,
            startedTimeAgo = "18m ago",
            totalDurationSeconds = 142,
            stages = listOf(
                PipelineStage("Code Lint & Static Analysis", PipelineStatus.SUCCESS, 24, listOf("ESLint: 0 errors, 0 warnings", "Typecheck passed in 8.2s")),
                PipelineStage("Unit & Concurrency Tests", PipelineStatus.SUCCESS, 45, listOf("Ran 128 tests across 6 suites", "Pytest OT resolution: PASSED")),
                PipelineStage("Enterprise SAST Security Scan", PipelineStatus.SUCCESS, 31, listOf("OWASP Top 10 Audit: 0 CVEs", "Zero-Trust policy verification passed")),
                PipelineStage("Multi-Arch Container Build", PipelineStatus.SUCCESS, 28, listOf("Built image gcr.io/codeforge-cloud/engine:7f8b92c (arm64, amd64)", "Digest: sha256:7f8b... verified")),
                PipelineStage("Multi-Region Cloud Deploy", PipelineStatus.SUCCESS, 14, listOf("Rolled out to us-east-1, eu-central-1, ap-southeast-1", "Traffic shifted 100% with zero downtime"))
            )
        ),
        PipelineRun(
            id = "pipe-9081",
            branch = "hotfix/jwt-auth",
            commitHash = "3e1a90f",
            commitMessage = "ci(pipeline): add zero-trust SAST scanner & multi-region deploy",
            triggeredBy = "Alex Rivera",
            status = PipelineStatus.SUCCESS,
            startedTimeAgo = "1h ago",
            totalDurationSeconds = 120,
            stages = listOf(
                PipelineStage("Code Lint & Static Analysis", PipelineStatus.SUCCESS, 20, listOf("Cargo clippy passed")),
                PipelineStage("Unit & Concurrency Tests", PipelineStatus.SUCCESS, 38, listOf("Rust JWT test suite passed")),
                PipelineStage("Enterprise SAST Security Scan", PipelineStatus.SUCCESS, 25, listOf("No high severity CVE detected")),
                PipelineStage("Multi-Arch Container Build", PipelineStatus.SUCCESS, 22, listOf("Docker layer cached")),
                PipelineStage("Multi-Region Cloud Deploy", PipelineStatus.SUCCESS, 15, listOf("Staging environment updated"))
            )
        )
    )

    private val _pipelines = MutableStateFlow(initialPipelines)
    val pipelines: StateFlow<List<PipelineRun>> = _pipelines.asStateFlow()

    // ==========================================
    // 6. Interactive Deployment Previews
    // ==========================================
    private val initialEnvironments = listOf(
        DeploymentEnvironment(
            name = "Production",
            url = "https://app.codeforge.cloud",
            port = 443,
            activeCommit = "7f8b92c",
            status = "Healthy",
            sslActive = true,
            lastDeployedAgo = "18m ago",
            activeReplicas = 12
        ),
        DeploymentEnvironment(
            name = "Staging",
            url = "https://staging.codeforge.cloud",
            port = 8080,
            activeCommit = "feature/realtime-collab",
            status = "Live",
            sslActive = true,
            lastDeployedAgo = "45m ago",
            activeReplicas = 4
        ),
        DeploymentEnvironment(
            name = "Preview Sandbox",
            url = "https://pr-42.preview.codeforge.internal",
            port = 3000,
            activeCommit = "HEAD~1",
            status = "Interactive",
            sslActive = true,
            lastDeployedAgo = "5m ago",
            activeReplicas = 2
        )
    )

    private val _environments = MutableStateFlow(initialEnvironments)
    val environments: StateFlow<List<DeploymentEnvironment>> = _environments.asStateFlow()

    private val _activePreviewEnv = MutableStateFlow(initialEnvironments[2])
    val activePreviewEnv: StateFlow<DeploymentEnvironment> = _activePreviewEnv.asStateFlow()

    // ==========================================
    // 7. Bug & Issue Tracking (DevCycle)
    // ==========================================
    private val initialIssues = listOf(
        IssueTicket(
            id = "CF-104",
            title = "Race condition in CRDT operational transform under 500+ peers",
            description = "When 500+ active peers send concurrent insert mutations to the same line within 10ms, operational transform order can momentarily desync.",
            severity = IssueSeverity.CRITICAL,
            status = IssueStatus.IN_PROGRESS,
            assigneeName = "Sarah Chen",
            assigneeColor = 0xFF00E5FF,
            linkedBranch = "feature/realtime-collab",
            linkedCommit = "7f8b92c",
            tags = listOf("concurrency", "crdt", "websockets"),
            createdAgo = "2h ago"
        ),
        IssueTicket(
            id = "CF-103",
            title = "Multi-region Raft follower latency spike in ap-southeast-1",
            description = "Under high cross-continental WAN load, replication heartbeat exceeds 120ms threshold, prompting spurious leader re-elections.",
            severity = IssueSeverity.HIGH,
            status = IssueStatus.IN_REVIEW,
            assigneeName = "Elena Rostov",
            assigneeColor = 0xFF10B981,
            linkedBranch = "main",
            linkedCommit = "9c21ef8",
            tags = listOf("raft", "multi-region", "database"),
            createdAgo = "5h ago"
        ),
        IssueTicket(
            id = "CF-102",
            title = "Enforce MFA for all Admin role permission mutations",
            description = "Enterprise compliance requires strict step-up authentication when granting Push-to-Main permissions to contributors.",
            severity = IssueSeverity.MEDIUM,
            status = IssueStatus.RESOLVED,
            assigneeName = "Alex Rivera",
            assigneeColor = 0xFFA855F7,
            linkedBranch = "hotfix/jwt-auth",
            linkedCommit = "3e1a90f",
            tags = listOf("security", "rbac", "zero-trust"),
            createdAgo = "1d ago"
        ),
        IssueTicket(
            id = "CF-101",
            title = "Add dark mode theme contrast for terminal syntax output",
            description = "Improve contrast ratio for warning tokens in bash shell output to meet WCAG AAA standards.",
            severity = IssueSeverity.LOW,
            status = IssueStatus.BACKLOG,
            assigneeName = "Marcus Vance",
            assigneeColor = 0xFFF59E0B,
            linkedBranch = null,
            linkedCommit = null,
            tags = listOf("ui", "accessibility"),
            createdAgo = "2d ago"
        )
    )

    private val _issues = MutableStateFlow(initialIssues)
    val issues: StateFlow<List<IssueTicket>> = _issues.asStateFlow()

    // ==========================================
    // 8. Microservices & Multi-Region Persistence
    // ==========================================
    private val initialMicroservices = listOf(
        MicroserviceNode("srv-gw", "API Gateway Mesh", "Envoy / EnvoyMesh", ServiceHealth.HEALTHY, 28, 480, 12, 14500, "v2.4.1"),
        MicroserviceNode("srv-auth", "Enterprise Auth & RBAC", "Rust Zero-Trust", ServiceHealth.HEALTHY, 18, 310, 4, 8200, "v2.4.0"),
        MicroserviceNode("srv-collab", "Real-Time Collab Sync", "Node.js WebSocket", ServiceHealth.HEALTHY, 44, 1120, 18, 22000, "v2.4.1"),
        MicroserviceNode("srv-db", "Postgres Distributed Shard", "CockroachDB Multi-Raft", ServiceHealth.HEALTHY, 52, 3400, 24, 6800, "v23.2"),
        MicroserviceNode("srv-cache", "Redis Memory Mesh", "Redis Cluster 7.2", ServiceHealth.HEALTHY, 22, 1850, 2, 42000, "v7.2.4"),
        MicroserviceNode("srv-build", "Isolated Sandbox Container", "gVisor RunSC", ServiceHealth.HEALTHY, 65, 2400, 35, 1200, "v1.8.0")
    )

    private val _microservices = MutableStateFlow(initialMicroservices)
    val microservices: StateFlow<List<MicroserviceNode>> = _microservices.asStateFlow()

    private val initialRegions = listOf(
        PersistenceRegion("us-east-1", "US East (N. Virginia)", isPrimaryLeader = true, ReplicationState.SYNCHRONIZED, 2, 284.5, true),
        PersistenceRegion("eu-central-1", "EU Central (Frankfurt)", isPrimaryLeader = false, ReplicationState.SYNCHRONIZED, 38, 284.5, true),
        PersistenceRegion("ap-southeast-1", "AP South (Singapore)", isPrimaryLeader = false, ReplicationState.SYNCHRONIZED, 72, 284.4, true)
    )

    private val _regions = MutableStateFlow(initialRegions)
    val regions: StateFlow<List<PersistenceRegion>> = _regions.asStateFlow()

    // ==========================================
    // 9. Enterprise Security & Audit State
    // ==========================================
    private val initialAuditLogs = listOf(
        SecurityAuditEntry("aud-1", "19:15:22", "sarah.c@codeforge.io", "PUSH_COMMIT: 7f8b92c to main", "192.168.1.42", "ALLOWED", true),
        SecurityAuditEntry("aud-2", "18:54:10", "alex.r@codeforge.io", "TRIGGER_PIPELINE: pipe-9082", "10.0.4.18", "ALLOWED", true),
        SecurityAuditEntry("aud-3", "18:30:05", "guest.bot@unverified", "ACCESS_SECRET_KEY: GITHUB_TOKEN", "203.0.113.88", "BLOCKED", false),
        SecurityAuditEntry("aud-4", "17:42:19", "elena.r@codeforge.io", "FAILOVER_REGION_CHECK: ap-southeast-1", "172.16.0.5", "VERIFIED", true)
    )

    private val _auditLogs = MutableStateFlow(initialAuditLogs)
    val auditLogs: StateFlow<List<SecurityAuditEntry>> = _auditLogs.asStateFlow()

    private val initialVulnerabilities = listOf(
        VulnerabilityAlert("CVE-2026-3819", "libcrypto3", IssueSeverity.HIGH, "v3.0.14-r1", "Potential timing attack vulnerability in ECDSA signature validation."),
        VulnerabilityAlert("CVE-2026-1194", "tar-stream", IssueSeverity.LOW, "v2.2.1", "Path traversal vulnerability during tarball decompression.")
    )

    private val _vulnerabilities = MutableStateFlow(initialVulnerabilities)
    val vulnerabilities: StateFlow<List<VulnerabilityAlert>> = _vulnerabilities.asStateFlow()

    // ==========================================
    // 10. Analytics & Telemetry State
    // ==========================================
    private val _telemetry = MutableStateFlow(
        EnvironmentTelemetry(
            environment = "Production (Global)",
            p50LatencyMs = 14,
            p95LatencyMs = 48,
            p99LatencyMs = 86,
            throughputRps = 38400,
            errorRatePercent = 0.004,
            uptimePercent = 99.995,
            activeCollaborators = 4
        )
    )
    val telemetry: StateFlow<EnvironmentTelemetry> = _telemetry.asStateFlow()

    // ==========================================
    // Actions & Methods
    // ==========================================

    fun selectFile(file: ProjectFile) {
        _activeFile.value = file
        if (!_openTabs.value.any { it.id == file.id }) {
            _openTabs.update { it + file }
        }
    }

    fun closeTab(file: ProjectFile) {
        val currentTabs = _openTabs.value
        if (currentTabs.size > 1) {
            val updated = currentTabs.filter { it.id != file.id }
            _openTabs.value = updated
            if (_activeFile.value.id == file.id) {
                _activeFile.value = updated.last()
            }
        }
    }

    fun updateFileContent(newContent: String) {
        val current = _activeFile.value
        val updated = current.copy(content = newContent, isModified = true)
        _activeFile.value = updated
        _files.update { list -> list.map { if (it.id == updated.id) updated else it } }
        _openTabs.update { list -> list.map { if (it.id == updated.id) updated else it } }
    }

    fun addNewFile(name: String, language: CodeLanguage) {
        val extension = language.extension
        val finalName = if (name.contains(".")) name else "$name.$extension"
        val newFile = ProjectFile(
            id = "file-${UUID.randomUUID().toString().take(6)}",
            name = finalName,
            path = "src/$finalName",
            language = language,
            content = "// New $name file\n",
            isModified = true
        )
        _files.update { it + newFile }
        selectFile(newFile)
        appendTerminalLine("$ touch src/$finalName", TerminalLineType.INPUT)
        appendTerminalLine("Created new file src/$finalName", TerminalLineType.SUCCESS)
    }

    fun toggleVoiceChannel() {
        _isVoiceActive.update { !it }
    }

    fun addComment(lineNum: Int, text: String) {
        val newComment = CodeComment(
            id = "c-${UUID.randomUUID().toString().take(6)}",
            authorName = "You (Lead Architect)",
            authorAvatarColor = 0xFF00E5FF,
            lineNumber = lineNum,
            comment = text,
            timestamp = "Just now"
        )
        _comments.update { it + newComment }
    }

    fun switchBranch(branchName: String) {
        _branches.update { list ->
            list.map { it.copy(isCurrent = (it.name == branchName)) }
        }
        appendTerminalLine("$ git checkout $branchName", TerminalLineType.INPUT)
        appendTerminalLine("Switched to branch '$branchName'", TerminalLineType.SUCCESS)
    }

    fun createBranch(newBranchName: String) {
        val branch = GitBranch(newBranchName, isDefault = false, isCurrent = true, aheadCount = 0, behindCount = 0)
        _branches.update { list ->
            list.map { it.copy(isCurrent = false) } + branch
        }
        appendTerminalLine("$ git checkout -b $newBranchName", TerminalLineType.INPUT)
        appendTerminalLine("Switched to a new branch '$newBranchName'", TerminalLineType.SUCCESS)
    }

    fun commitChanges(message: String) {
        val newHash = UUID.randomUUID().toString().replace("-", "").take(11)
        val currentBranch = _branches.value.firstOrNull { it.isCurrent }?.name ?: "main"
        val newCommit = GitCommit(
            hash = newHash,
            message = message,
            author = "You (Lead Architect)",
            authorColor = 0xFF00E5FF,
            relativeTime = "Just now",
            branch = currentBranch,
            additions = 32,
            deletions = 5,
            filesChanged = listOf(_activeFile.value.path)
        )
        _commits.update { listOf(newCommit) + it }
        _stagedFiles.update { emptyList() }
        _files.update { list -> list.map { it.copy(isModified = false) } }

        appendTerminalLine("$ git commit -m \"$message\"", TerminalLineType.INPUT)
        appendTerminalLine("[$currentBranch ${newCommit.shortHash}] $message\n 1 file changed, 32 insertions(+), 5 deletions(-)", TerminalLineType.SUCCESS)

        addAuditLog("COMMIT", "Committed $newHash to $currentBranch: $message")
        triggerPipeline(newHash, message, currentBranch)
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        appendTerminalLine("$ $trimmed", TerminalLineType.INPUT)

        val parts = trimmed.split("\\s+".toRegex())
        val command = parts.getOrNull(0)?.lowercase() ?: ""

        when (command) {
            "help" -> {
                appendTerminalLine("""
Available CodeForge Commands:
  git status                  - Show working tree status
  git commit -m "<msg>"       - Record changes to repository
  git checkout <branch>       - Switch branches
  git branch                  - List branches
  git push                    - Push commits to cloud remote
  npm test / cargo test       - Run full test suite in container
  npm run build               - Trigger multi-arch container build
  python <file>               - Execute Python script
  docker ps                   - List running microservice containers
  kubectl get pods            - Inspect production Kubernetes cluster
  curl <url>                  - Inspect live microservice HTTP response
  failover                    - Test multi-region database failover
  clear                       - Clear terminal screen
  whoami                      - Show authenticated enterprise role
""".trimIndent(), TerminalLineType.STDOUT)
            }
            "clear" -> {
                _terminalLogs.value = emptyList()
            }
            "whoami" -> {
                appendTerminalLine("lead-architect@codeforge.cloud (Role: ADMIN / OWNER, MFA: ACTIVE)", TerminalLineType.STDOUT)
            }
            "pwd" -> {
                appendTerminalLine("/workspace/codeforge-enterprise", TerminalLineType.STDOUT)
            }
            "ls" -> {
                val fileList = _files.value.joinToString("   ") { it.name }
                appendTerminalLine("src/   k8s/   services/   tests/   Dockerfile   $fileList", TerminalLineType.STDOUT)
            }
            "git" -> {
                val subCmd = parts.getOrNull(1)?.lowercase()
                when (subCmd) {
                    "status" -> {
                        val branch = _branches.value.firstOrNull { it.isCurrent }?.name ?: "main"
                        val modified = _files.value.filter { it.isModified }
                        if (modified.isEmpty()) {
                            appendTerminalLine("On branch $branch\nnothing to commit, working tree clean", TerminalLineType.STDOUT)
                        } else {
                            appendTerminalLine("On branch $branch\nChanges not staged for commit:\n" + modified.joinToString("\n") { "  modified: ${it.path}" }, TerminalLineType.STDOUT)
                        }
                    }
                    "branch" -> {
                        val branchList = _branches.value.joinToString("\n") { (if (it.isCurrent) "* " else "  ") + it.name }
                        appendTerminalLine(branchList, TerminalLineType.STDOUT)
                    }
                    "push" -> {
                        appendTerminalLine("Enumerating objects: 12, done.\nCounting objects: 100% (12/12), done.\nWriting objects: 100% (12/12), 3.42 KiB | 3.42 MiB/s, done.\nTo https://github.com/codeforge/codeforge-enterprise.git\n   main -> main", TerminalLineType.SUCCESS)
                    }
                    "checkout" -> {
                        val target = parts.getOrNull(2)
                        if (target != null) {
                            switchBranch(target)
                        } else {
                            appendTerminalLine("fatal: missing branch name", TerminalLineType.STDERR)
                        }
                    }
                    "log" -> {
                        val logs = _commits.value.take(3).joinToString("\n\n") {
                            "commit ${it.hash}\nAuthor: ${it.author}\nDate:   ${it.relativeTime}\n\n    ${it.message}"
                        }
                        appendTerminalLine(logs, TerminalLineType.STDOUT)
                    }
                    else -> appendTerminalLine("git: '${subCmd ?: ""}' executed. Type 'help' for usage.", TerminalLineType.STDOUT)
                }
            }
            "npm" -> {
                val sub = parts.getOrNull(1)
                if (sub == "test") {
                    appendTerminalLine("PASS src/__tests__/server.test.ts\nPASS src/__tests__/auth.test.ts\nTest Suites: 2 passed, 2 total\nTests:       18 passed, 18 total\nSnapshots:   0 total\nTime:        1.42s", TerminalLineType.SUCCESS)
                } else if (sub == "run" && parts.getOrNull(2) == "build") {
                    appendTerminalLine("vite v5.2.0 building for production...\n✓ 142 modules transformed.\ndist/server.js   84.2 kB │ gzip: 24.1 kB\n✓ built in 620ms", TerminalLineType.SUCCESS)
                } else {
                    appendTerminalLine("npm command completed successfully.", TerminalLineType.STDOUT)
                }
            }
            "cargo" -> {
                appendTerminalLine("   Compiling codeforge-auth v0.1.0 (/workspace/src/auth)\n    Finished `test` profile [unoptimized + debuginfo] in 0.88s\n     Running unittests src/auth/jwt.rs\n\ntest verify_enterprise_token_valid ... ok\ntest reject_unauthorized_scope ... ok\n\ntest result: ok. 2 passed; 0 failed; finished in 0.04s", TerminalLineType.SUCCESS)
            }
            "docker" -> {
                appendTerminalLine("CONTAINER ID   IMAGE                               COMMAND                  CREATED          STATUS          PORTS                    NAMES\n9a8f112b3c4d   gcr.io/codeforge-cloud/engine:2.4   \"docker-entrypoint.s…\"   24 minutes ago   Up 24 minutes   0.0.0.0:8080->8080/tcp   codeforge-engine-mesh\n1b2c3d4e5f6a   redis:7.2-alpine                    \"docker-entrypoint.s…\"   2 hours ago      Up 2 hours      0.0.0.0:6379->6379/tcp   codeforge-redis-cache", TerminalLineType.STDOUT)
            }
            "kubectl" -> {
                appendTerminalLine("NAME                                    READY   STATUS    RESTARTS   AGE     IP            NODE\ncodeforge-engine-67d4f9b88-4p2x9       1/1     Running   0          18m     10.244.1.42   node-pool-us-east-1a\ncodeforge-engine-67d4f9b88-8k1w2       1/1     Running   0          18m     10.244.2.18   node-pool-us-east-1b\ncodeforge-engine-67d4f9b88-m9z7q       1/1     Running   0          18m     10.244.3.99   node-pool-us-east-1c", TerminalLineType.STDOUT)
            }
            "failover" -> {
                triggerFailoverTest()
            }
            else -> {
                appendTerminalLine("codeforge-sh: command executed: $trimmed (exit code 0)", TerminalLineType.INFO)
            }
        }
    }

    private fun appendTerminalLine(text: String, type: TerminalLineType) {
        val line = TerminalLine(
            id = "t-${UUID.randomUUID().toString().take(6)}",
            text = text,
            type = type,
            timestamp = timeFormat.format(Date())
        )
        _terminalLogs.update { it + line }
    }

    fun triggerPipeline(commitHash: String = "HEAD", commitMsg: String = "Manual workflow trigger", branch: String = "main") {
        val newPipe = PipelineRun(
            id = "pipe-${UUID.randomUUID().toString().take(4)}",
            branch = branch,
            commitHash = commitHash.take(7),
            commitMessage = commitMsg,
            triggeredBy = "You (Manual/GitHook)",
            status = PipelineStatus.RUNNING,
            startedTimeAgo = "Just now",
            totalDurationSeconds = 0,
            stages = listOf(
                PipelineStage("Code Lint & Static Analysis", PipelineStatus.RUNNING, 8, listOf("Running ESLint & Cargo clippy...")),
                PipelineStage("Unit & Concurrency Tests", PipelineStatus.QUEUED, 0, emptyList()),
                PipelineStage("Enterprise SAST Security Scan", PipelineStatus.QUEUED, 0, emptyList()),
                PipelineStage("Multi-Arch Container Build", PipelineStatus.QUEUED, 0, emptyList()),
                PipelineStage("Multi-Region Cloud Deploy", PipelineStatus.QUEUED, 0, emptyList())
            )
        )
        _pipelines.update { listOf(newPipe) + it }

        // Simulate real-time pipeline execution progress in background
        scope.launch {
            delay(1200)
            _pipelines.update { list ->
                list.map { pipe ->
                    if (pipe.id == newPipe.id) {
                        pipe.copy(
                            stages = pipe.stages.mapIndexed { idx, stage ->
                                when (idx) {
                                    0 -> stage.copy(status = PipelineStatus.SUCCESS, logs = listOf("ESLint: 0 warnings", "Cargo clippy: OK"))
                                    1 -> stage.copy(status = PipelineStatus.RUNNING, durationSeconds = 14, logs = listOf("Running 128 tests..."))
                                    else -> stage
                                }
                            }
                        )
                    } else pipe
                }
            }
            delay(1500)
            _pipelines.update { list ->
                list.map { pipe ->
                    if (pipe.id == newPipe.id) {
                        pipe.copy(
                            status = PipelineStatus.SUCCESS,
                            totalDurationSeconds = 48,
                            stages = pipe.stages.map { it.copy(status = PipelineStatus.SUCCESS, durationSeconds = 12, logs = listOf("Stage completed successfully")) }
                        )
                    } else pipe
                }
            }
        }
    }

    fun updateIssueStatus(issueId: String, newStatus: IssueStatus) {
        _issues.update { list ->
            list.map { if (it.id == issueId) it.copy(status = newStatus) else it }
        }
        addAuditLog("ISSUE_UPDATE", "Updated issue $issueId status to ${newStatus.label}")
    }

    fun createIssue(title: String, desc: String, severity: IssueSeverity) {
        val newIssue = IssueTicket(
            id = "CF-${(105..999).random()}",
            title = title,
            description = desc,
            severity = severity,
            status = IssueStatus.BACKLOG,
            assigneeName = "Sarah Chen",
            assigneeColor = 0xFF00E5FF,
            linkedBranch = "main",
            tags = listOf("devops", "cloud"),
            createdAgo = "Just now"
        )
        _issues.update { listOf(newIssue) + it }
        addAuditLog("CREATE_ISSUE", "Filed new issue ${newIssue.id}: $title")
    }

    fun triggerFailoverTest() {
        val currentLeader = _regions.value.firstOrNull { it.isPrimaryLeader }?.regionCode ?: "us-east-1"
        val nextLeader = if (currentLeader == "us-east-1") "eu-central-1" else "us-east-1"

        appendTerminalLine("[Failover] Initiating Raft leader step-down in $currentLeader...", TerminalLineType.SYSTEM)
        appendTerminalLine("[Failover] Promoting $nextLeader to Primary Leader with zero data loss...", TerminalLineType.INFO)

        _regions.update { list ->
            list.map { reg ->
                reg.copy(isPrimaryLeader = (reg.regionCode == nextLeader))
            }
        }

        appendTerminalLine("[Failover] Successfully promoted $nextLeader. Multi-region consensus intact.", TerminalLineType.SUCCESS)
        addAuditLog("FAILOVER_TRIGGER", "Failover executed: leader shifted to $nextLeader")
    }

    fun updateCollaboratorRole(userId: String, newRole: UserRole) {
        _collaborators.update { list ->
            list.map { if (it.id == userId) it.copy(role = newRole) else it }
        }
        addAuditLog("RBAC_ROLE_CHANGE", "Updated user $userId to role ${newRole.title}")
    }

    fun selectPreviewEnvironment(env: DeploymentEnvironment) {
        _activePreviewEnv.value = env
    }

    // ==========================================
    // 11. AI Connectors (Claude, ChatGPT, Grok, etc.)
    // ==========================================
    private val initialConnectors = listOf(
        AIConnector(
            id = "conn-claude",
            name = "Claude 3.5 Sonnet",
            provider = AIConnectorProvider.CLAUDE,
            endpointUrl = "https://api.anthropic.com/v1/messages",
            apiKey = "",
            modelName = "claude-3-5-sonnet-20241022",
            isEnabled = true,
            isDefault = true,
            latencyMs = 38,
            lastTestedStatus = "Connected"
        ),
        AIConnector(
            id = "conn-chatgpt",
            name = "OpenAI GPT-4o",
            provider = AIConnectorProvider.CHATGPT,
            endpointUrl = "https://api.openai.com/v1/chat/completions",
            apiKey = "",
            modelName = "gpt-4o",
            isEnabled = true,
            isDefault = false,
            latencyMs = 45,
            lastTestedStatus = "Ready"
        ),
        AIConnector(
            id = "conn-grok",
            name = "xAI Grok-2",
            provider = AIConnectorProvider.GROK,
            endpointUrl = "https://api.x.ai/v1/chat/completions",
            apiKey = "",
            modelName = "grok-2-latest",
            isEnabled = true,
            isDefault = false,
            latencyMs = 52,
            lastTestedStatus = "Ready"
        ),
        AIConnector(
            id = "conn-local",
            name = "Ollama Local Mesh",
            provider = AIConnectorProvider.CUSTOM,
            endpointUrl = "http://localhost:11434/api/generate",
            apiKey = "local-mesh-token",
            modelName = "deepseek-coder:6.7b",
            isEnabled = false,
            isDefault = false,
            latencyMs = 12,
            lastTestedStatus = "Offline / Local Standby"
        )
    )

    private val _connectors = MutableStateFlow(initialConnectors)
    val connectors: StateFlow<List<AIConnector>> = _connectors.asStateFlow()

    fun addConnector(connector: AIConnector) {
        _connectors.update { it + connector }
        addAuditLog("CONNECTOR_ADDED", "Added model connector ${connector.name}")
    }

    fun toggleConnector(connectorId: String, enabled: Boolean) {
        _connectors.update { list ->
            list.map { if (it.id == connectorId) it.copy(isEnabled = enabled) else it }
        }
    }

    fun setDefaultConnector(connectorId: String) {
        _connectors.update { list ->
            list.map { it.copy(isDefault = (it.id == connectorId)) }
        }
    }

    fun updateConnectorKey(connectorId: String, apiKey: String, modelName: String) {
        _connectors.update { list ->
            list.map {
                if (it.id == connectorId) it.copy(apiKey = apiKey, modelName = modelName, lastTestedStatus = "Connected")
                else it
            }
        }
    }

    // ==========================================
    // 12. Online & Offline Mode
    // ==========================================
    private val _networkStatus = MutableStateFlow(NetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    fun toggleOfflineMode() {
        val current = _networkStatus.value
        val newManualOffline = !current.isManuallyOffline
        _networkStatus.value = current.copy(
            isManuallyOffline = newManualOffline,
            isOnline = !newManualOffline,
            activeConnectionType = if (newManualOffline) "Local Sandbox (Offline)" else "Wi-Fi Cloud Mesh (Online)"
        )
        val statusText = if (newManualOffline) "OFFLINE MODE ENABLED" else "ONLINE CLOUD MESH RECONNECTED"
        appendTerminalLine("[Network] $statusText", TerminalLineType.INFO)
    }

    fun setNetworkConnectivity(isOnline: Boolean) {
        val current = _networkStatus.value
        if (!current.isManuallyOffline) {
            _networkStatus.value = current.copy(isOnline = isOnline)
        }
    }

    // ==========================================
    // 13. Auto-Correction Pipeline (Gemini Powered)
    // ==========================================
    private val autoCorrectionService = com.example.data.service.GeminiAutoCorrectionService()

    private val _autoCorrectionResult = MutableStateFlow<AutoCorrectionResult?>(null)
    val autoCorrectionResult: StateFlow<AutoCorrectionResult?> = _autoCorrectionResult.asStateFlow()

    private val _isAutoCorrecting = MutableStateFlow(false)
    val isAutoCorrecting: StateFlow<Boolean> = _isAutoCorrecting.asStateFlow()

    fun autoCorrectActiveFile() {
        val file = _activeFile.value
        val isOnline = _networkStatus.value.isOnline
        val activeConn = _connectors.value.firstOrNull { it.isDefault && it.isEnabled }

        _isAutoCorrecting.value = true
        appendTerminalLine("[Auto-Fix] Analyzing ${file.name} with DevEngine Auto-Correction pipeline...", TerminalLineType.INFO)

        scope.launch {
            try {
                val result = autoCorrectionService.autoCorrectCode(
                    fileId = file.id,
                    code = file.content,
                    language = file.language,
                    isOnline = isOnline,
                    activeConnector = activeConn
                )
                _autoCorrectionResult.value = result
                appendTerminalLine("[Auto-Fix] Repair completed: ${result.explanation}", TerminalLineType.SUCCESS)
                addAuditLog("AUTO_CORRECTION", "Auto-corrected ${file.name} (${result.engineName})")
            } catch (e: Exception) {
                appendTerminalLine("[Auto-Fix] Completed local heuristic verification.", TerminalLineType.INFO)
            } finally {
                _isAutoCorrecting.value = false
            }
        }
    }

    fun applyAutoCorrection() {
        val result = _autoCorrectionResult.value ?: return
        updateFileContent(result.correctedCode)
        _autoCorrectionResult.value = null
        appendTerminalLine("[Auto-Fix] Applied verified corrections to active buffer.", TerminalLineType.SUCCESS)
    }

    fun dismissAutoCorrection() {
        _autoCorrectionResult.value = null
    }

    // ==========================================
    // 14. Direct GitHub Integration
    // ==========================================
    private val gitHubService = com.example.data.service.GitHubPushService()

    private val _gitHubConfig = MutableStateFlow(GitHubConfig())
    val gitHubConfig: StateFlow<GitHubConfig> = _gitHubConfig.asStateFlow()

    private val _lastGitHubPush = MutableStateFlow<GitHubPushResult?>(null)
    val lastGitHubPush: StateFlow<GitHubPushResult?> = _lastGitHubPush.asStateFlow()

    private val _isPushingToGitHub = MutableStateFlow(false)
    val isPushingToGitHub: StateFlow<Boolean> = _isPushingToGitHub.asStateFlow()

    fun updateGitHubConfig(config: GitHubConfig) {
        _gitHubConfig.value = config
        addAuditLog("GITHUB_CONFIG", "Updated GitHub repository target: ${config.repoOwner}/${config.repoName}")
    }

    fun pushDirectlyToGitHub(commitMsg: String = "Update workspace files via CodeForge") {
        val config = _gitHubConfig.value
        val files = _files.value
        val currentBranch = _branches.value.firstOrNull { it.isCurrent }?.name ?: "main"
        val isOnline = _networkStatus.value.isOnline

        _isPushingToGitHub.value = true
        appendTerminalLine("[GitHub] Initiating direct push to https://github.com/${config.repoOwner}/${config.repoName} ($currentBranch)...", TerminalLineType.INFO)

        scope.launch {
            try {
                val result = gitHubService.pushToGitHub(
                    config = config,
                    files = files,
                    commitMessage = commitMsg,
                    branchName = currentBranch,
                    isOnline = isOnline
                )
                _lastGitHubPush.value = result
                _gitHubConfig.update { it.copy(lastPushedCommitSha = result.commitSha, lastPushedAt = result.timestamp) }

                if (isOnline) {
                    appendTerminalLine("[GitHub] ✓ ${result.message}", TerminalLineType.SUCCESS)
                    addAuditLog("GITHUB_PUSH", "Pushed ${result.commitSha} to GitHub")
                } else {
                    appendTerminalLine("[GitHub] (Offline) Queued push for branch $currentBranch", TerminalLineType.INFO)
                }
            } catch (e: Exception) {
                appendTerminalLine("[GitHub] Push queued locally: ${e.message}", TerminalLineType.INFO)
            } finally {
                _isPushingToGitHub.value = false
            }
        }
    }

    private fun addAuditLog(action: String, details: String) {
        val log = SecurityAuditEntry(
            id = "aud-${UUID.randomUUID().toString().take(6)}",
            timestamp = timeFormat.format(Date()),
            actor = "lead-architect@codeforge.cloud",
            action = "$action: $details",
            ipAddress = "192.168.1.100",
            status = "ALLOWED",
            isMfaVerified = true
        )
        _auditLogs.update { listOf(log) + it }
    }
}
