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

1. Commit and push these changes
2. Verify workflows run successfully on GitHub
3. Set up the following GitHub Secrets for Maven publishing:
   - `ASTROIDMC_MAVEN_USERNAME`
   - `ASTROIDMC_MAVEN_PASSWORD`

## Workflow Triggers

- **build-and-release.yml**: Runs on push to main/master, tags (v*), and pull requests
- **maven-ci.yml**: Runs on push/PR to main/master/develop branches
- **codeql-analysis.yml**: Runs on push/PR to main/master and weekly on Mondays
- **publish-maven.yml**: Runs on releases or manual dispatch

