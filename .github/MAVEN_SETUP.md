# GitHub Secrets Setup for Maven Publishing

To enable automatic publishing to your Maven repository, you need to configure GitHub Secrets.

## 🔐 Required Secrets

Add these secrets to your GitHub repository:

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add the following secrets:

### `MAVEN_USERNAME`
- **Value**: Your AstroidMC Maven repository username
- **Description**: Username for authenticating to maven.astroidmc.com

### `MAVEN_PASSWORD`
- **Value**: Your AstroidMC Maven repository password or token
- **Description**: Password/token for authenticating to maven.astroidmc.com

---

## 📋 Step-by-Step Setup

### 1. Get Your Maven Credentials

Contact your Maven repository administrator or check your account settings on:
```
https://maven.astroidmc.com
```

### 2. Add Secrets to GitHub

#### Via GitHub Web Interface:

1. Navigate to: `https://github.com/YOUR_USERNAME/NetworkDataAPI/settings/secrets/actions`
2. Click **"New repository secret"**
3. Add first secret:
   - **Name**: `MAVEN_USERNAME`
   - **Secret**: `your-maven-username`
   - Click **"Add secret"**
4. Add second secret:
   - **Name**: `MAVEN_PASSWORD`
   - **Secret**: `your-maven-password-or-token`
   - Click **"Add secret"**

#### Via GitHub CLI (gh):

```bash
# Install GitHub CLI if not already installed
# https://cli.github.com/

# Login to GitHub
gh auth login

# Add secrets
gh secret set MAVEN_USERNAME --body "your-maven-username"
gh secret set MAVEN_PASSWORD --body "your-maven-password"
```

---

## 🚀 Publishing to Maven

### Automatic Publishing (Recommended)

Once secrets are configured, publishing happens automatically:

#### For Snapshot Versions:
```bash
# Just push to main/master
git push origin main
```
→ Publishes to maven-snapshots repository

#### For Release Versions:
```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```
→ Publishes to maven-releases repository

### Manual Publishing

You can also trigger publishing manually:

1. Go to **Actions** tab on GitHub
2. Select **"Publish to Maven"** workflow
3. Click **"Run workflow"**
4. Choose deploy type (snapshot or release)
5. Click **"Run workflow"**

---

## 🔧 Local Publishing (Optional)

To publish from your local machine:

### 1. Create Maven settings.xml

Create or edit `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>astroidmc-releases</id>
            <username>YOUR_USERNAME</username>
            <password>YOUR_PASSWORD</password>
        </server>
        <server>
            <id>astroidmc-snapshots</id>
            <username>YOUR_USERNAME</username>
            <password>YOUR_PASSWORD</password>
        </server>
    </servers>
</settings>
```

### 2. Deploy

```bash
# Deploy snapshot
mvn clean deploy

# Deploy release (update version first)
mvn versions:set -DnewVersion=1.0.0
mvn clean deploy
```

---

## ✅ Verifying the Setup

### Check if Secrets are Added:

1. Go to repository **Settings** → **Secrets and variables** → **Actions**
2. You should see:
   - ✅ `MAVEN_USERNAME`
   - ✅ `MAVEN_PASSWORD`

### Test the Workflow:

1. Make a small change and push to main
2. Go to **Actions** tab
3. Check if **"Publish to Maven"** workflow runs successfully
4. Verify artifact appears in your Maven repository

---

## 🐛 Troubleshooting

### "Error: Unauthorized"
- Check that `MAVEN_USERNAME` and `MAVEN_PASSWORD` are correct
- Verify credentials work by logging into maven.astroidmc.com
- Make sure the account has write permissions

### "Error: Repository not found"
- Verify the repository URLs in `pom.xml` are correct
- Check that the Maven repository exists
- Ensure you have access to the repository

### Secrets Not Working
- Secret names are case-sensitive (use exact names)
- Secrets don't have leading/trailing spaces
- Re-create secrets if in doubt
- Check workflow logs for specific errors

### Manual Testing
Test your credentials locally first:
```bash
# Test authentication
curl -u YOUR_USERNAME:YOUR_PASSWORD https://maven.astroidmc.com/repository/maven-releases/
```

---

## 🔒 Security Best Practices

1. **Never commit credentials** to Git
2. **Use tokens instead of passwords** when possible
3. **Rotate credentials** periodically
4. **Limit permissions** to only what's needed
5. **Use environment-specific credentials** (dev vs prod)
6. **Enable two-factor authentication** on your Maven account

---

## 📚 Additional Resources

- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Maven Deploy Plugin](https://maven.apache.org/plugins/maven-deploy-plugin/)
- [GitHub CLI](https://cli.github.com/)

---

## ✔️ Setup Complete Checklist

- [ ] Maven repository account created
- [ ] Repository URLs added to `pom.xml`
- [ ] `MAVEN_USERNAME` secret added to GitHub
- [ ] `MAVEN_PASSWORD` secret added to GitHub
- [ ] Test push to verify workflow runs
- [ ] Verify artifact appears in Maven repository
- [ ] Update `MAVEN_DEPENDENCY_GUIDE.md` with correct repository URLs

---

**You're all set for automatic Maven publishing!** 🚀

Every push to main and every release will now automatically publish to your Maven repository!

