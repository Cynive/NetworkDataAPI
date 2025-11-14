# Workflow templates for NetworkDataAPI

This directory contains GitHub Actions workflows for automated CI/CD.

## 🔄 Available Workflows

### 1. **Build and Release** (`build-and-release.yml`)
Main workflow for building and releasing the project.

**Triggers:**
- Push to `main` or `master` branch **only when**:
  - `.java` files change
  - `pom.xml` files change
  - Workflow file itself changes
- Push with version tag (e.g., `v1.0.0`) → Creates official release
- Pull requests
- Manual trigger via GitHub Actions UI

**Features:**
- ✅ Builds both Paper and Bungee JARs
- ✅ Runs tests
- ✅ Uploads artifacts
- ✅ Creates GitHub releases automatically
- ✅ Attaches JARs to releases
- ✅ Smart path filtering - only builds when code changes

### 2. **Maven CI** (`maven-ci.yml`)
Continuous Integration workflow for testing.

**Triggers:**
- Push to `main`, `master`, or `develop` **only when**:
  - `.java` files change
  - `pom.xml` files change
  - Workflow file itself changes
- Pull requests
- Manual trigger via GitHub Actions UI

**Features:**
- ✅ Tests on multiple OS (Ubuntu, Windows)
- ✅ Tests on Java 21
- ✅ Caches Maven dependencies
- ✅ Generates test reports
- ✅ Archives artifacts
- ✅ Smart path filtering - skips builds for README/docs changes

### 3. **CodeQL Analysis** (`codeql-analysis.yml`)
Security scanning workflow.

**Triggers:**
- Push to `main` or `master` **only when**:
  - `.java` files change
  - `pom.xml` files change
- Pull requests (only for code changes)
- Weekly schedule (Monday midnight)
- Manual trigger via GitHub Actions UI

**Features:**
- ✅ Automated security scanning
- ✅ Finds vulnerabilities
- ✅ Security advisories
- ✅ Smart path filtering - only scans when code changes

### 4. **Dependency Review** (`dependency-review.yml`)
Checks dependencies for security issues.

**Triggers:**
- Pull requests

**Features:**
- ✅ Reviews dependency changes
- ✅ Checks for known vulnerabilities
- ✅ Comments on PRs

### 5. **Publish to Maven** (`publish-maven.yml`)
Publishes artifacts to AstroidMC Maven repository.

**Triggers:**
- Release creation
- Manual trigger via GitHub Actions UI

---

## 🚀 How to Trigger Workflows

### Option 1: Automatic Triggers (Smart)
The workflows now use **path filters** - they only run when relevant files change:

**This WILL trigger builds:**
```bash
# Changing Java code
git add src/
git commit -m "fix: bug in player data service"
git push

# Changing pom.xml
git add pom.xml
git commit -m "chore: update dependencies"
git push
```

**This will NOT trigger builds:**
```bash
# Updating documentation only
git add README.md
git commit -m "docs: update installation guide"
git push

# Updating example configs
git add example-config.yml
git commit -m "docs: add example config"
git push
```

### Option 2: Manual Trigger
1. Go to GitHub → Actions tab
2. Select the workflow you want to run
3. Click "Run workflow" button
4. Optionally add a reason
5. Click "Run workflow"

### Option 3: Version Release
Create and push a version tag for official releases:
```bash
# Update version in pom.xml files first!
git tag v1.0.0
git push origin v1.0.0
```
→ Creates an official release with version `1.0.0`

---

## 📝 Path Filter Details

Workflows now ignore changes to:
- ✅ `*.md` files (README, documentation)
- ✅ `.yml` config files (except pom.xml and workflow files)
- ✅ `.gitignore`, LICENSE, etc.
- ✅ Documentation folders
- ✅ Example files

Workflows WILL run for changes to:
- 🔨 `*.java` files
- 🔨 `pom.xml` files
- 🔨 Workflow `.yml` files themselves

---

## 📦 Release Artifacts

Each release includes:
- `NetworkDataAPI-Paper-{version}.jar` - For Paper/Spigot servers
- `NetworkDataAPI-Bungee-{version}.jar` - For BungeeCord proxies

---

## 🔧 Configuration

### Required Secrets
No additional secrets required! Uses GitHub's built-in `GITHUB_TOKEN`.

### Optional: Custom Release Notes
Edit the release body in `build-and-release.yml` under the "Create Release" step.

---

## 📊 Status Badges

Add these to your README.md:

```markdown
[![Build](https://github.com/YOUR_USERNAME/NetworkDataAPI/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/YOUR_USERNAME/NetworkDataAPI/actions/workflows/build-and-release.yml)
[![CodeQL](https://github.com/YOUR_USERNAME/NetworkDataAPI/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/YOUR_USERNAME/NetworkDataAPI/actions/workflows/codeql-analysis.yml)
```

---

## 🎯 Workflow Diagram

```
Push to main/master
    ↓
Build & Test (Maven CI)
    ↓
Create Development Build
    ↓
Upload to "latest" release
```

```
Push version tag (v1.0.0)
    ↓
Build & Test
    ↓
Create Official Release
    ↓
Attach JARs to release
    ↓
Publish release notes
```

---

## ⚙️ Customization

### Change Java Version
Edit in workflow files:
```yaml
java-version: '17'  # Change to 21 if needed
```

### Change Maven Goals
Edit build command:
```yaml
run: mvn clean package -DskipTests  # Add or remove flags
```

### Disable Tests
Add to Maven command:
```yaml
run: mvn clean package -DskipTests -Dmaven.test.skip=true
```

---

## 🐛 Troubleshooting

### Build Fails
1. Check Java version compatibility
2. Verify Maven dependencies are accessible
3. Check logs in Actions tab

### Release Not Created
1. Ensure tag starts with `v` (e.g., `v1.0.0`)
2. Check GITHUB_TOKEN permissions
3. Verify workflow file syntax

### Artifacts Missing
1. Check file paths in workflow
2. Ensure build completes successfully
3. Verify upload-artifact step runs

---

**All workflows are ready to use!** Push your code to see them in action! 🚀

