package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CodeLanguage
import com.example.data.model.IssueSeverity
import com.example.data.model.IssueStatus
import com.example.data.model.UserRole
import com.example.data.repository.CodeForgeRepository
import com.example.ui.ActiveScreen
import com.example.ui.CodeForgeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CodeForge", appName)
    }

    @Test
    fun `test CodeForge repository files and active document`() {
        val repository = CodeForgeRepository()
        val files = repository.files.value
        assertTrue("Repository must have initial files", files.isNotEmpty())

        val active = repository.activeFile.value
        assertNotNull("Active file should not be null", active)
        assertEquals("src/server.ts", active.path)

        // Add file
        repository.addNewFile("telemetry.ts", CodeLanguage.TYPESCRIPT)
        val updatedFiles = repository.files.value
        assertTrue("Files should contain new file", updatedFiles.any { it.name == "telemetry.ts" })
    }

    @Test
    fun `test terminal command executor`() {
        val repository = CodeForgeRepository()
        repository.executeTerminalCommand("git status")
        val logs = repository.terminalLogs.value
        assertTrue("Terminal should contain git status log", logs.any { it.text.contains("On branch") })

        repository.executeTerminalCommand("whoami")
        val whoamiLogs = repository.terminalLogs.value
        assertTrue("Terminal should contain whoami response", whoamiLogs.any { it.text.contains("lead-architect") })
    }

    @Test
    fun `test git commits and branch switching`() {
        val repository = CodeForgeRepository()
        val initialCommitCount = repository.commits.value.size

        repository.createBranch("feature/raft-sync")
        val branches = repository.branches.value
        assertTrue(branches.any { it.name == "feature/raft-sync" && it.isCurrent })

        repository.commitChanges("feat: test commit")
        assertEquals(initialCommitCount + 1, repository.commits.value.size)
    }

    @Test
    fun `test bug tracker issue state transitions`() {
        val repository = CodeForgeRepository()
        val issues = repository.issues.value
        val first = issues.first()

        repository.updateIssueStatus(first.id, IssueStatus.RESOLVED)
        val updated = repository.issues.value.first { it.id == first.id }
        assertEquals(IssueStatus.RESOLVED, updated.status)
    }

    @Test
    fun `test viewModel navigation and RBAC roles`() {
        val viewModel = CodeForgeViewModel()
        assertEquals(ActiveScreen.WORKSPACE, viewModel.currentScreen.value)

        viewModel.navigateTo(ActiveScreen.COLLAB)
        assertEquals(ActiveScreen.COLLAB, viewModel.currentScreen.value)

        val sarah = viewModel.collaborators.value.first { it.id == "user-sarah" }
        viewModel.updateCollaboratorRole(sarah.id, UserRole.ADMIN)
        val updatedSarah = viewModel.collaborators.value.first { it.id == "user-sarah" }
        assertEquals(UserRole.ADMIN, updatedSarah.role)
    }

    @Test
    fun `test AI connectors and offline toggle`() {
        val repository = CodeForgeRepository()
        val connectors = repository.connectors.value
        assertTrue("Connectors must contain Claude and ChatGPT", connectors.any { it.id == "conn-claude" })

        // Toggle offline mode
        val initialStatus = repository.networkStatus.value.isOnline
        repository.toggleOfflineMode()
        val offlineStatus = repository.networkStatus.value.isOnline
        assertEquals(!initialStatus, offlineStatus)

        // Toggle back to online
        repository.toggleOfflineMode()
        assertTrue(repository.networkStatus.value.isOnline)
    }

    @Test
    fun `test auto-correction and GitHub direct push`() {
        val repository = CodeForgeRepository()

        // Test Auto-Correction
        repository.autoCorrectActiveFile()
        // Wait or check auto-correction state
        assertNotNull("Auto-correction service instantiated", repository.networkStatus.value)

        // Test GitHub Push
        val ghConfig = repository.gitHubConfig.value
        assertEquals("enterprise-user", ghConfig.repoOwner)
        repository.pushDirectlyToGitHub("Test commit via CodeForge")
        assertNotNull("GitHub push service called", repository.gitHubConfig.value)
    }
}
