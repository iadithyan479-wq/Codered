# CodeForge — Collaborative Cloud IDE & Engineering Cockpit

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_14+-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin_2.1-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose M3" />
  <img src="https://img.shields.io/badge/Design-Claude_Minimal-D97757?style=for-the-badge" alt="Claude Design" />
  <img src="https://img.shields.io/badge/Version-v1.0.0--Release-10B981?style=for-the-badge" alt="Release" />
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge" alt="License" />
</p>

---

## ✦ Overview

**CodeForge** is an enterprise-grade mobile cloud IDE and engineering cockpit designed with a minimalist **Claude AI-inspired aesthetic**. It empowers engineers and development teams to write, test, debug, collaborate, and deploy cloud applications directly from their mobile and tablet devices with zero friction.

Equipped with an intelligent **DevEngine Auto-Correction** pipeline, multi-model AI connectors (**Claude, ChatGPT, Grok, and Local LLMs**), an embedded interactive Linux shell terminal, real-time pair programming, full Git version control with direct GitHub synchronization, multi-stage CI/CD pipelines, interactive deployment previews, and multi-region microservice persistence.

---

## 🌟 Key Features

### 1. ✦ Claude AI Minimalist UX/UI
- **Warm Editorial Aesthetic**: Warm charcoal dark palette (`#191816`, `#22211E`), terracotta primary accents (`#D97757`), subtle warm borders (`#383631`), soft sage green indicators (`#729B79`), and warm cream typography (`#F3F1EC`).
- **Distraction-Free Layout**: Pill badges, soft rounded cards (12–16dp), floating action sheets, and clean monospace gutters.

### 2. ✦ DevEngine Intelligent Auto-Correction (Gemini-Powered)
- **Seamless & Invisible AI**: Operates under the hood using `gemini-3.5-flash` through an internal neural pipeline—presented cleanly to users as native **Auto-Fix** without exposing third-party AI branding.
- **1-Tap Code Repair**: Detects syntax errors, missing standard imports, unbalanced brackets, typing errors, and edge-case bugs.
- **Diff & Proposal Card**: Generates concise fix explanations with an interactive **"Accept & Apply Fix"** or **"Dismiss"** dialog.
- **Offline Heuristics**: Automatically switches to local rule-based heuristics when offline to repair unclosed braces and malformed expressions without internet.

### 3. ✦ Multi-Model AI Connectors Hub
- **Plug-and-Play AI**: Connect to **Claude (Anthropic)**, **ChatGPT (OpenAI)**, **Grok (xAI)**, or **Local/Ollama Mesh**.
- **Credentials & Model Configuration**: Enter custom API keys, select model versions (`claude-3-5-sonnet`, `gpt-4o`, `grok-2`), and test live connection latency.
- **Primary Engine Selection**: Set which connector serves as your default pair-programming engine.

### 4. ✦ Direct GitHub Push & Version Control
- **1-Click Remote Push**: Push your workspace directly to any GitHub repository and branch with commit messages and Personal Access Token (PAT) authentication.
- **Full Git Staging**: View staged and unstaged files, diffs, branch history graph, and commit logs.
- **Offline Commit Queue**: Work while offline; commits and pushes are queued locally and automatically synced upon reconnecting.

### 5. ✦ Real-Time Pair Programming & RBAC
- **Collaborator Presence**: Live peer avatars, active line badges, cursor indicators, typing notifications, and file locks.
- **Role-Based Access Control**: Configurable permission tiers for **Owner**, **Admin**, **Maintainer**, **Contributor**, and **Viewer**.
- **Voice Channel**: Integrated audio room toggle for rapid voice sync during pairing sessions.

### 6. ✦ Built-In Interactive Terminal
- **Cloud & Linux Shell**: Interactive command execution supporting `npm`, `git`, `python`, `cargo`, `docker`, `kubectl`, `terraform`, and `whoami`.
- **Slide-Up Drawer**: Toggle terminal drawer directly while editing code or view fullscreen.
- **Execution Matrix**: Displays ANSI-styled output, exit codes, process statuses, and execution timestamps.

### 7. ✦ Automated CI/CD Pipelines & Live Previews
- **Multi-Stage Workflows**: Visual execution pipeline with Lint, Security Audit, Unit Tests, Container Build, and Deployment stages.
- **Responsive Previews**: Live interactive viewport simulation across Mobile (360dp), Tablet (540dp), and Desktop viewports with SSL indicators and replica health monitors.

### 8. ✦ Microservices & Multi-Region Persistence
- **Service Mesh Monitor**: Real-time CPU, memory, and latency metrics for API Gateway, Auth Service, Sync Engine, and Postgres clusters.
- **Multi-Region Consensus**: Geographic replication across `us-east-1`, `eu-central-1`, and `ap-southeast-1` with 1-tap zero-downtime failover simulation.

---

## 🏗️ Architecture & Tech Stack

```text
┌────────────────────────────────────────────────────────┐
│                      CodeForge App                     │
│  (Jetpack Compose M3 • Claude Minimal UI Design System) │
└──────────────────────────┬─────────────────────────────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    ▼                      ▼                      ▼
┌──────────────┐   ┌──────────────┐   ┌───────────────────┐
│ Code Editor  │   │  Interactive │   │  Version Control  │
│  & Gutter    │   │   Terminal   │   │  & GitHub Sync    │
└──────┬───────┘   └──────┬───────┘   └─────────┬─────────┘
       │                  │                     │
       └──────────────────┼─────────────────────┘
                          ▼
┌────────────────────────────────────────────────────────┐
│                 CodeForgeViewModel                     │
│            (StateFlow & Coroutine Scopes)              │
└─────────────────────────┬──────────────────────────────┘
                          ▼
┌────────────────────────────────────────────────────────┐
│                CodeForgeRepository                     │
│    (Workspace State, Local Persistence & Git Engine)   │
└──────┬──────────────────┬──────────────────┬───────────┘
       ▼                  ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────────┐
│ Gemini Auto │    │ GitHub REST │    │ AI Connectors   │
│ Correction  │    │ Direct Push │    │ (Claude/OpenAI) │
└─────────────┘    └─────────────┘    └─────────────────┘
```

- **UI Framework**: Android Jetpack Compose with Material 3
- **State Management**: Kotlin Coroutines, Flow, StateFlow, ViewModel
- **Networking**: OkHttp 4, Retrofit 2, Moshi
- **Local Persistence**: Offline-first repository architecture with Room / SQLite compatibility
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 or JDK 21
- Android SDK 36 (Android 15)
- Android device or emulator running Android 7.0+ (API 24+)

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/CodeForge.git
   cd CodeForge
   ```

2. **Configure Environment Secrets (Optional for Live Gemini API):**
   Copy `.env.example` to `.env` and insert your Gemini API Key:
   ```bash
   cp .env.example .env
   # Edit .env and uncomment GEMINI_API_KEY:
   # GEMINI_API_KEY=AIzaSy...
   ```

3. **Build the Debug APK:**
   ```bash
   gradle assembleDebug
   ```

4. **Run Unit & Robolectric Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

5. **Install on Connected Device / Emulator:**
   ```bash
   gradle installDebug
   ```

---

## 📦 Releases & CI/CD

CodeForge includes automated GitHub Actions workflows in `.github/workflows/`:
- **`ci.yml`**: Runs lint and Robolectric unit tests on every pull request and push to `main`.
- **`release.yml`**: Automatically builds the release APK and creates a new GitHub Release with attached binaries whenever a version tag (e.g. `v1.0.0`) is published.

---

## 📄 License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.
