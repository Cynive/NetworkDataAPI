# Contributing to NetworkDataAPI

First off, thank you for considering contributing to NetworkDataAPI! 🎉

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)

---

## 📜 Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please be respectful and constructive in all interactions.

---

## 🤝 How Can I Contribute?

### Reporting Bugs
If you find a bug:
1. Check if it's already reported in [Issues](https://github.com/YOUR_USERNAME/NetworkDataAPI/issues)
2. If not, create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Server version, Java version
   - Relevant logs/screenshots

### Suggesting Features
Feature suggestions are welcome! Please:
1. Check existing feature requests
2. Create a new issue with:
   - Clear use case
   - Expected benefits
   - Possible implementation approach

### Improving Documentation
- Fix typos or clarify existing docs
- Add examples
- Translate documentation
- Update outdated information

### Contributing Code
See [Development Setup](#development-setup) and [Pull Request Process](#pull-request-process) below.

---

## 🔧 Development Setup

### Prerequisites
- **JDK 17 or higher** (JDK 21 recommended)
- **Maven 3.6+**
- **Git**
- **IDE** (IntelliJ IDEA recommended)
- **MongoDB** (for testing, optional)

### Setup Steps

1. **Fork the repository**
   ```bash
   # Click "Fork" on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/NetworkDataAPI.git
   cd NetworkDataAPI
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_USERNAME/NetworkDataAPI.git
   ```

4. **Build the project**
   ```bash
   mvn clean package
   ```

5. **Import into IDE**
   - IntelliJ: File → Open → Select `pom.xml`
   - Eclipse: File → Import → Existing Maven Projects

### Project Structure
```
NetworkDataAPI/
├── networkdataapi-core/     # Core logic (database, cache, API)
├── networkdataapi-paper/    # Paper/Spigot implementation
├── networkdataapi-bungee/   # BungeeCord implementation
└── .github/workflows/       # CI/CD workflows
```

---

## 🔀 Pull Request Process

### Before You Start
1. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

2. **Keep your fork updated**
   ```bash
   git fetch upstream
   git merge upstream/main
   ```

### Making Changes

1. **Write clean, documented code**
   - Follow existing code style
   - Add JavaDoc for public methods
   - Write self-documenting code

2. **Test your changes**
   ```bash
   mvn clean test
   mvn clean package
   ```

3. **Test on real server** (if applicable)
   - Test on Paper/Spigot
   - Test on BungeeCord
   - Verify no breaking changes

### Submitting PR

1. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

2. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create Pull Request**
   - Go to your fork on GitHub
   - Click "New Pull Request"
   - Fill in the template:
     - **Title**: Clear, concise description
     - **Description**: What, why, how
     - **Related Issues**: Link related issues
     - **Testing**: How you tested it
     - **Breaking Changes**: List any breaking changes

4. **Wait for review**
   - Address review comments
   - Update your PR if needed
   - Be patient and respectful

### PR Requirements
- ✅ All CI checks pass
- ✅ Code follows style guidelines
- ✅ New code has JavaDoc
- ✅ No merge conflicts
- ✅ Tested on both Paper and BungeeCord (if applicable)

---

## 📝 Coding Standards

### Java Style
Follow standard Java conventions:
- **Indentation**: 4 spaces (no tabs)
- **Line length**: Max 120 characters
- **Braces**: Same line for methods/classes
- **Naming**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Variables: `camelCase`

### JavaDoc
Document all public classes and methods:
```java
/**
 * Brief description of what this does.
 * 
 * <p>Longer description with more details if needed.</p>
 * 
 * @param parameter description of parameter
 * @return description of return value
 * @throws ExceptionType when this exception is thrown
 * @since 1.0
 */
public ReturnType methodName(ParameterType parameter) {
    // implementation
}
```

### Code Organization
- One class per file
- Logical method ordering
- Group related methods together
- Use meaningful variable names
- Avoid magic numbers (use constants)

### Thread Safety
- Use `@ThreadSafe` annotation when applicable
- Document thread-safety guarantees
- Use proper synchronization
- Prefer immutability

### Error Handling
- Don't swallow exceptions
- Log errors appropriately
- Provide meaningful error messages
- Use try-with-resources

---

## 💬 Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding/updating tests
- `chore`: Build/tooling changes
- `ci`: CI/CD changes

### Examples
```bash
feat(core): add player data caching
fix(rest): resolve type conversion issue in updateData
docs(api): update API usage examples
refactor(database): simplify connection pooling logic
perf(cache): optimize cache eviction strategy
```

### Subject Line
- Use imperative mood ("add" not "added")
- Don't capitalize first letter
- No period at the end
- Max 50 characters

### Body
- Explain what and why, not how
- Wrap at 72 characters
- Separate from subject with blank line

### Footer
- Reference issues: `Fixes #123`, `Closes #456`
- Breaking changes: `BREAKING CHANGE: description`

---

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=PlayerDataServiceTest

# Run with coverage
mvn test jacoco:report
```

### Writing Tests
- Place tests in `src/test/java`
- Name test classes `*Test.java`
- Use descriptive test method names
- Test both success and failure cases
- Mock external dependencies

---

## 📚 Additional Resources

- [API Documentation](API_DOCUMENTATION.md)
- [Quick Start Guide](QUICK_START.md)
- [Project Summary](PROJECT_SUMMARY.md)
- [Workflow Documentation](.github/workflows/README.md)

---

## ❓ Questions?

- Open a [Discussion](https://github.com/YOUR_USERNAME/NetworkDataAPI/discussions)
- Join our Discord (if available)
- Check existing issues and PRs

---

## 🎉 Thank You!

Your contributions make this project better for everyone in the Minecraft community!

**Happy coding! 🚀**

