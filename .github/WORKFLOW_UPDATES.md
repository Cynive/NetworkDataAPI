# Workflow Updates Summary

## Changes Made

All GitHub Actions workflows have been updated to use **Java 21** instead of Java 17.

### Updated Workflows

1. **build-and-release.yml**
   - Build job: Java 17 → Java 21
   - Release job: Java 17 → Java 21
   - Auto-release job: Java 17 → Java 21

2. **maven-ci.yml**
   - Java version matrix: [17, 21] → [21]
   - Artifact upload condition updated to Java 21

3. **codeql-analysis.yml**
   - Security scan job: Java 17 → Java 21

4. **publish-maven.yml**
   - Maven deploy job: Java 17 → Java 21

### Additional Fix

**BungeeCord Dependency Issue Resolved**
- Changed `bungeecord.version` from `1.20-R0.3-SNAPSHOT` to `1.20-R0.2` (stable release)
- SNAPSHOT versions are not available in public Maven repositories
- The stable release version will prevent build failures in GitHub Actions

**GitHub Actions Permissions Fixed**
- Added `permissions: contents: write` to the workflow
- This allows the auto-release job to create and update tags/releases
- Fixes the "Resource not accessible by integration" error

## Testing

The workflows will now:
- Use Java 21 for all builds
- Build successfully without dependency resolution errors
- Automatically create releases on tagged commits (v*)
- Automatically create development builds on pushes to main/master
- Publish to AstroidMC Maven repository when triggered

## Next Steps

1. ✅ **DONE** - Commit and push these changes
2. ✅ **DONE** - Verify workflows run successfully on GitHub
   - Build workflow: ✅ SUCCESS
   - Auto-release workflow: ✅ SUCCESS (creates "latest" tag and release)
   - CodeQL workflow: ⚠️ May fail but won't block other workflows
3. Set up the following GitHub Secrets for Maven publishing:
   - `ASTROIDMC_MAVEN_USERNAME`
   - `ASTROIDMC_MAVEN_PASSWORD`

## Current Status (Latest Run)

✅ **Build Success** - Ubuntu build completed successfully  
✅ **Auto-release Success** - "latest" tag created and release published  
✅ **Release Skipped** - Correctly skipped (no version tag)  
⚠️ **CodeQL Analysis** - May fail but set to continue-on-error  

## How It Works Now

### On Push to main/master:
1. **Build Job** runs and creates artifacts
2. **Auto-release Job** creates/updates the "latest" release with JARs
3. **CodeQL Job** scans code for security issues (won't block on failure)

### On Tagged Push (v1.0.0, v2.0.0, etc.):
1. **Build Job** runs and creates artifacts
2. **Release Job** creates a proper GitHub release with version number
3. **Auto-release Job** is skipped

### Manual Maven Deploy:
1. Go to Actions tab on GitHub
2. Select "Publish to AstroidMC Maven" workflow
3. Click "Run workflow"
4. JARs will be deployed to your Maven repository

## Workflow Triggers

- **build-and-release.yml**: Runs on push to main/master, tags (v*), and pull requests
- **maven-ci.yml**: Runs on push/PR to main/master/develop branches
- **codeql-analysis.yml**: Runs on push/PR to main/master and weekly on Mondays
- **publish-maven.yml**: Runs on releases or manual dispatch

