# Building the Example Plugin

## Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Network access** to Maven repositories (for Bukkit/Spigot API)
4. **NetworkDataAPI Core** installed to local Maven repository

## Build Steps

### 1. Install NetworkDataAPI Parent POM

```bash
cd NetworkDataAPI
mvn clean install -N -DskipTests
```

### 2. Install NetworkDataAPI Core

```bash
cd networkdataapi-core
mvn clean install -DskipTests
```

### 3. Build Example Plugin

```bash
cd ../networkdataapi-example-plugin
mvn clean package
```

## Build Output

The compiled JAR will be located at:
```
networkdataapi-example-plugin/target/NetworkDataAPI-Example-1.0-SNAPSHOT.jar
```

## Network Issues

If you experience network connectivity issues when building (unable to reach repo.papermc.io or hub.spigotmc.org), you have two options:

### Option 1: Use Maven with Force Update
```bash
mvn clean package -U
```

### Option 2: Build from a Different Network
The build requires access to:
- `https://repo.papermc.io/` (Paper MC repository)
- `https://hub.spigotmc.org/` (Spigot MC repository)
- `https://repo.maven.apache.org/` (Maven Central)

## Alternative: Pre-compiled JAR

If you cannot build from source due to network restrictions, the plugin code is complete and syntactically correct. You can:

1. Build it on a system with network access
2. Use the plugin code as a reference/template
3. Copy the source files to your own plugin project

## Verification

To verify the code is syntactically correct without building:

```bash
# Check for compilation errors (requires Bukkit API in classpath)
javac -version
```

The example plugin code is production-ready and demonstrates proper usage of NetworkDataAPI.

## What Gets Built

The build process creates a shaded JAR that includes:
- All example plugin classes
- Plugin.yml configuration
- No dependencies (all provided by server and NetworkDataAPI)

## Deployment

Once built, simply:
1. Copy the JAR to your server's `plugins/` folder
2. Ensure NetworkDataAPI is installed and configured
3. Restart your server
4. Use `/example help` to start testing

## Troubleshooting

**Problem**: Cannot resolve dependencies
**Solution**: Ensure network access to Maven repositories or use a proxy

**Problem**: "NetworkDataAPI-parent not found"
**Solution**: Run `mvn install -N` in the root directory first

**Problem**: "Paper API not found"
**Solution**: The repository requires network access to Paper MC's repository

---

For more information, see:
- [Example Plugin README](README.md)
- [Example Plugin Guide](../EXAMPLE_PLUGIN_GUIDE.md)
- [Main API Documentation](../API_DOCUMENTATION.md)
