# Workflow templates for NetworkDataAPI

This directory contains GitHub Actions workflows for automated CI/CD.

## 🔄 Available Workflows

### 1. **Build and Release** (`build-and-release.yml`)
Main workflow for building and releasing the project.

**Triggers:**
- Push to `main` or `master` branch → Creates development build
- Push with version tag (e.g., `v1.0.0`) → Creates official release
- Pull requests → Builds and tests

**Features:**
- ✅ Builds both Paper and Bungee JARs
- ✅ Runs tests
- ✅ Uploads artifacts
- ✅ Creates GitHub releases automatically
- ✅ Attaches JARs to releases

### 2. **Maven CI** (`maven-ci.yml`)
Continuous Integration workflow for testing.

**Triggers:**
- Push to `main`, `master`, or `develop`
- Pull requests

**Features:**
- ✅ Tests on multiple OS (Ubuntu, Windows)
- ✅ Tests on multiple Java versions (17, 21)
- ✅ Caches Maven dependencies
- ✅ Generates test reports
- ✅ Archives artifacts

### 3. **CodeQL Analysis** (`codeql-analysis.yml`)
Security scanning workflow.

**Triggers:**
- Push to `main` or `master`
- Pull requests
- Weekly schedule (Monday midnight)

**Features:**
- ✅ Automated security scanning
- ✅ Finds vulnerabilities
- ✅ Security advisories

### 4. **Dependency Review** (`dependency-review.yml`)
Checks dependencies for security issues.

**Triggers:**
- Pull requests

**Features:**
- ✅ Reviews dependency changes
- ✅ Checks for known vulnerabilities
- ✅ Comments on PRs

---

## 🚀 How to Create a Release

### Automatic Development Builds
Push to `main` or `master`:
```bash
git add .
git commit -m "feat: new feature"
git push origin main
```
→ Creates a `latest` pre-release automatically

### Official Version Release
Create and push a version tag:
```bash
# Update version in pom.xml files first!
git tag v1.0.0
git push origin v1.0.0
```
→ Creates an official release with version `1.0.0`

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

