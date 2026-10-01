package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CodeForgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

enum class ActiveScreen(val title: String, val iconName: String) {
    WORKSPACE("Editor", "Code"),
    TERMINAL("Terminal", "Terminal"),
    GIT("Version Control", "Commit"),
    CONNECTORS("AI Connectors", "Extension"),
    COLLAB("Pair Collab & RBAC", "People"),
    PIPELINES("CI/CD Workflows", "Speed"),
    DEPLOYMENTS("Live Previews", "Rocket"),
    MICROSERVICES("Microservices & Cloud", "Cloud"),
    ISSUES("Bug Tracker", "BugReport"),
    SECURITY("Security & Analytics", "Security")
}

class CodeForgeViewModel(
    val repository: CodeForgeRepository = CodeForgeRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ActiveScreen.WORKSPACE)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    // File / Workspace state
    val files: StateFlow<List<ProjectFile>> = repository.files
    val activeFile: StateFlow<ProjectFile> = repository.activeFile
    val openTabs: StateFlow<List<ProjectFile>> = repository.openTabs

    // Collaborators & Presence
    val collaborators: StateFlow<List<Collaborator>> = repository.collaborators
    val isVoiceActive: StateFlow<Boolean> = repository.isVoiceActive
    val comments: StateFlow<List<CodeComment>> = repository.comments

    // Git Version Control
    val branches: StateFlow<List<GitBranch>> = repository.branches
    val commits: StateFlow<List<GitCommit>> = repository.commits
    val stagedFiles: StateFlow<List<StagedFile>> = repository.stagedFiles

    // Terminal
    val terminalLogs: StateFlow<List<TerminalLine>> = repository.terminalLogs

    // CI/CD Pipelines
    val pipelines: StateFlow<List<PipelineRun>> = repository.pipelines

    // Deployments
    val environments: StateFlow<List<DeploymentEnvironment>> = repository.environments
    val activePreviewEnv: StateFlow<DeploymentEnvironment> = repository.activePreviewEnv

    // Issues
    val issues: StateFlow<List<IssueTicket>> = repository.issues

    // Microservices & Persistence
    val microservices: StateFlow<List<MicroserviceNode>> = repository.microservices
    val regions: StateFlow<List<PersistenceRegion>> = repository.regions

    // Security & Audit
    val auditLogs: StateFlow<List<SecurityAuditEntry>> = repository.auditLogs
    val vulnerabilities: StateFlow<List<VulnerabilityAlert>> = repository.vulnerabilities

    // Analytics
    val telemetry: StateFlow<EnvironmentTelemetry> = repository.telemetry

    // AI Connectors (Claude, ChatGPT, Grok, etc.)
    val connectors: StateFlow<List<AIConnector>> = repository.connectors

    // Online & Offline State
    val networkStatus: StateFlow<NetworkStatus> = repository.networkStatus

    // Auto-Correction Engine
    val autoCorrectionResult: StateFlow<AutoCorrectionResult?> = repository.autoCorrectionResult
    val isAutoCorrecting: StateFlow<Boolean> = repository.isAutoCorrecting

    // GitHub Integration
    val gitHubConfig: StateFlow<GitHubConfig> = repository.gitHubConfig
    val lastGitHubPush: StateFlow<GitHubPushResult?> = repository.lastGitHubPush
    val isPushingToGitHub: StateFlow<Boolean> = repository.isPushingToGitHub

    // Navigation and UI Dialog states
    private val _isTerminalDrawerOpen = MutableStateFlow(false)
    val isTerminalDrawerOpen: StateFlow<Boolean> = _isTerminalDrawerOpen.asStateFlow()

    private val _showNewFileDialog = MutableStateFlow(false)
    val showNewFileDialog: StateFlow<Boolean> = _showNewFileDialog.asStateFlow()

    private val _showNewIssueDialog = MutableStateFlow(false)
    val showNewIssueDialog: StateFlow<Boolean> = _showNewIssueDialog.asStateFlow()

    private val _showCommitDialog = MutableStateFlow(false)
    val showCommitDialog: StateFlow<Boolean> = _showCommitDialog.asStateFlow()

    private val _showInviteDialog = MutableStateFlow(false)
    val showInviteDialog: StateFlow<Boolean> = _showInviteDialog.asStateFlow()

    private val _showGitHubPushDialog = MutableStateFlow(false)
    val showGitHubPushDialog: StateFlow<Boolean> = _showGitHubPushDialog.asStateFlow()

    fun navigateTo(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    fun toggleTerminalDrawer() {
        _isTerminalDrawerOpen.value = !_isTerminalDrawerOpen.value
    }

    fun setTerminalDrawer(open: Boolean) {
        _isTerminalDrawerOpen.value = open
    }

    fun setShowNewFileDialog(show: Boolean) {
        _showNewFileDialog.value = show
    }

    fun setShowNewIssueDialog(show: Boolean) {
        _showNewIssueDialog.value = show
    }

    fun setShowCommitDialog(show: Boolean) {
        _showCommitDialog.value = show
    }

    fun setShowInviteDialog(show: Boolean) {
        _showInviteDialog.value = show
    }

    fun setShowGitHubPushDialog(show: Boolean) {
        _showGitHubPushDialog.value = show
    }

    // Repository operations
    fun selectFile(file: ProjectFile) = repository.selectFile(file)
    fun closeTab(file: ProjectFile) = repository.closeTab(file)
    fun updateCode(newCode: String) = repository.updateFileContent(newCode)
    fun addNewFile(name: String, language: CodeLanguage) = repository.addNewFile(name, language)

    fun toggleVoiceChannel() = repository.toggleVoiceChannel()
    fun addComment(lineNum: Int, text: String) = repository.addComment(lineNum, text)

    fun switchBranch(branchName: String) = repository.switchBranch(branchName)
    fun createBranch(newBranchName: String) = repository.createBranch(newBranchName)
    fun commitChanges(message: String) = repository.commitChanges(message)

    fun executeTerminalCommand(command: String) = repository.executeTerminalCommand(command)

    fun triggerPipeline() {
        val hash = commits.value.firstOrNull()?.shortHash ?: "HEAD"
        repository.triggerPipeline(commitHash = hash, commitMsg = "Manual CI/CD build run")
    }

    fun updateIssueStatus(issueId: String, status: IssueStatus) = repository.updateIssueStatus(issueId, status)
    fun createIssue(title: String, desc: String, severity: IssueSeverity) = repository.createIssue(title, desc, severity)

    fun triggerFailoverTest() = repository.triggerFailoverTest()
    fun updateCollaboratorRole(userId: String, newRole: UserRole) = repository.updateCollaboratorRole(userId, newRole)
    fun selectPreviewEnvironment(env: DeploymentEnvironment) = repository.selectPreviewEnvironment(env)

    // Auto-Correction Methods
    fun autoCorrectActiveFile() = repository.autoCorrectActiveFile()
    fun applyAutoCorrection() = repository.applyAutoCorrection()
    fun dismissAutoCorrection() = repository.dismissAutoCorrection()

    // Model Connectors Methods
    fun addConnector(connector: AIConnector) = repository.addConnector(connector)
    fun toggleConnector(id: String, enabled: Boolean) = repository.toggleConnector(id, enabled)
    fun setDefaultConnector(id: String) = repository.setDefaultConnector(id)
    fun updateConnectorKey(id: String, apiKey: String, modelName: String) = repository.updateConnectorKey(id, apiKey, modelName)

    // Online & Offline
    fun toggleOfflineMode() = repository.toggleOfflineMode()

    // GitHub Push Direct
    fun updateGitHubConfig(config: GitHubConfig) = repository.updateGitHubConfig(config)
    fun pushDirectlyToGitHub(commitMsg: String = "Update workspace files via CodeForge") = repository.pushDirectlyToGitHub(commitMsg)
}
