# Security Policy

## 🔒 Supported Versions

We release patches for security vulnerabilities in the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## 🐛 Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow these steps:

### 1. **DO NOT** open a public issue
Security vulnerabilities should be reported privately to prevent exploitation.

### 2. Report via GitHub Security Advisories
1. Go to the [Security tab](https://github.com/YOUR_USERNAME/NetworkDataAPI/security)
2. Click "Report a vulnerability"
3. Fill in the details

### 3. Or email directly
Send details to: **security@yourproject.com** (replace with your email)

### What to include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)
- Your contact information

## 🔍 What Happens Next?

1. **Acknowledgment**: We'll acknowledge receipt within 48 hours
2. **Investigation**: We'll investigate and determine severity
3. **Fix**: We'll work on a fix and keep you updated
4. **Release**: We'll release a patched version
5. **Credit**: You'll be credited (if desired) in the release notes

## ⏱️ Expected Timeline

- Initial response: **48 hours**
- Status update: **7 days**
- Fix release: **30 days** (for high severity)

## 🛡️ Security Best Practices

### For Users

#### MongoDB Security
```yaml
# Use authentication
mongodb:
  username: "your-username"
  password: "strong-password"
  
# Don't expose MongoDB publicly
# Bind to localhost only in mongod.conf:
net:
  bindIp: 127.0.0.1
```

#### REST API Security
```yaml
# Always use API keys
rest-api:
  enabled: true
  api-key: "generate-strong-random-key"
  
# Restrict by IP
  allowed-ips:
    - "127.0.0.1"
    - "your-server-ip"
```

#### File Permissions
```bash
# Restrict config file permissions
chmod 600 plugins/NetworkDataAPI/config.yml
```

### For Developers

#### Dependency Management
- Keep dependencies updated
- Review dependency changes in PRs
- Use `mvn dependency:tree` to check for vulnerabilities

#### Code Review
- All PRs require review
- Security-sensitive changes need extra scrutiny
- Run CodeQL scans before merging

#### Secrets Management
- Never commit credentials
- Use environment variables for sensitive data
- Rotate API keys regularly

## 🔐 Known Security Considerations

### MongoDB Connections
- Connection strings may contain credentials
- Config files should have restricted permissions
- Use encrypted connections when possible

### REST API
- API keys transmitted in headers
- Consider using HTTPS in production
- Rate limiting not implemented (consider adding)

### Caching
- Cached data is stored in memory
- Cache doesn't encrypt sensitive data
- Clear cache on plugin disable

## 📋 Security Checklist

Before deploying:
- [ ] MongoDB authentication enabled
- [ ] Strong passwords/API keys used
- [ ] Config file permissions restricted
- [ ] REST API IP whitelist configured
- [ ] Latest version installed
- [ ] Dependencies up to date
- [ ] Logs reviewed for suspicious activity

## 🏆 Hall of Fame

We appreciate security researchers who responsibly disclose vulnerabilities:

<!-- List security researchers who reported vulnerabilities -->
- *No reports yet - be the first!*

## 📞 Contact

For security concerns:
- GitHub Security Advisories (preferred)
- Email: security@yourproject.com

For general questions:
- GitHub Issues
- Discussions

---

**Thank you for helping keep NetworkDataAPI secure!** 🔒

