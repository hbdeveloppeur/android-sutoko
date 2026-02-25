# Ntfy Android Library

An Android Kotlin library for sending notifications via [ntfy.sh](https://ntfy.sh).

Inspired by [x00/ntfy-php](https://packagist.org/packages/x00/ntfy-php).

## Features

- Simple API for sending notifications
- Built-in support for error, log, and urgent notification channels
- Asynchronous operations with coroutines
- Configurable via BuildConfig fields
- Silent mode for production environments

## Installation

### Step 1: Add JitPack Repository (for publishing)

In your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add this for JitPack
    }
}
```

### Step 2: Configure BuildConfig Fields

In your app's `build.gradle.kts` (or the module using this library):

```kotlin
android {
    defaultConfig {
        // Define your ntfy channel IDs
        buildConfigField("String", "NTFY_ERROR_CHANNEL", "\"your-error-channel-id\"")
        buildConfigField("String", "NTFY_LOG_CHANNEL", "\"your-log-channel-id\"")
        buildConfigField("String", "NTFY_URGENT_CHANNEL", "\"your-urgent-channel-id\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

Or use `gradle.properties`:

```properties
NTFY_ERROR_CHANNEL=your-error-channel-id
NTFY_LOG_CHANNEL=your-log-channel-id
NTFY_URGENT_CHANNEL=your-urgent-channel-id
```

### Step 3: Add Dependency

```kotlin
dependencies {
    implementation("com.purpletear:ntfy:1.0.0")
}
```

## Usage

### Basic Setup

```kotlin
import com.purpletear.ntfy.Ntfy
import com.purpletear.ntfy.NtfyClient
import com.purpletear.ntfy.NtfyConfig

// Using BuildConfig (recommended)
val ntfy: Ntfy = NtfyClient(
    config = NtfyConfig.fromBuildConfig()
)

// Or manual configuration
val ntfy: Ntfy = NtfyClient(
    config = NtfyConfig(
        errorChannelId = "your-error-channel-id",
        logChannelId = "your-log-channel-id",
        urgentChannelId = "your-urgent-channel-id",
        baseUrl = "https://ntfy.sh",
        silent = false  // Set to true to swallow exceptions
    )
)
```

### Regular Notifications

```kotlin
// Send to default log channel
ntfy.send(message = "Something happened")

// Send with data
ntfy.send(
    message = "Something happened",
    data = mapOf("key" to "value", "user_id" to 123)
)

// Send to specific channel
ntfy.send(
    message = "Something happened",
    channelId = "my-custom-channel-id",
    data = mapOf("key" to "value")
)
```

### Exception Notifications

```kotlin
try {
    // Your code that might throw
    riskyOperation()
} catch (e: Exception) {
    ntfy.exception(
        throwable = e,
        data = mapOf("user_id" to 123, "context" to "foo")
    )
}
```

### Urgent Notifications

```kotlin
// Send urgent message
ntfy.urgent(message = "Server is down!")

// Send urgent exception
ntfy.urgent(
    throwable = Exception("Server is down!"),
    data = mapOf("server" to "production-01")
)
```

### Dependency Injection (Hilt Example)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NtfyModule {

    @Provides
    @Singleton
    fun provideNtfy(): Ntfy {
        return NtfyClient(
            config = NtfyConfig.fromBuildConfig(
                silent = BuildConfig.DEBUG.not()  // Silent in production
            )
        )
    }
}

// Usage in ViewModel or Repository
@HiltViewModel
class MyViewModel @Inject constructor(
    private val ntfy: Ntfy
) : ViewModel() {
    
    fun doSomething() {
        try {
            // ...
        } catch (e: Exception) {
            ntfy.exception(e, mapOf("screen" to "home"))
        }
    }
}
```

## Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `errorChannelId` | Channel ID for error notifications | `""` |
| `logChannelId` | Channel ID for log notifications | `""` |
| `urgentChannelId` | Channel ID for urgent notifications | `""` |
| `baseUrl` | ntfy.sh server URL | `https://ntfy.sh` |
| `silent` | If true, exceptions are swallowed | `false` |

## Publishing Tutorial

### Option 1: Publish to Maven Central (Sonatype)

#### Step 1: Create Sonatype Account

1. Create an account at [https://issues.sonatype.org](https://issues.sonatype.org)
2. Create a new project ticket for your group ID

#### Step 2: Generate GPG Key

```bash
# Generate key
gpg --gen-key

# List keys
gpg --list-keys

# Distribute key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

#### Step 3: Update build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

// ...

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.purpletear"
            artifactId = "ntfy"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Ntfy Android")
                description.set("Android library for ntfy.sh notifications")
                url.set("https://github.com/yourusername/ntfy-android")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("yourusername")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/yourusername/ntfy-android.git")
                    developerConnection.set("scm:git:ssh://github.com:yourusername/ntfy-android.git")
                    url.set("https://github.com/yourusername/ntfy-android")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: ""
                password = findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
```

#### Step 4: Add Credentials to local.properties

```properties
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password
signing.keyId=your_gpg_key_id
signing.password=your_gpg_password
signing.secretKeyRingFile=/Users/you/.gnupg/secring.gpg
```

#### Step 5: Publish

```bash
./gradlew :ntfy:publishReleasePublicationToSonatypeRepository
```

Then go to [https://s01.oss.sonatype.org](https://s01.oss.sonatype.org) to close and release the staging repository.

---

### Option 2: Publish to JitPack (Easiest)

#### Step 1: Push to GitHub

Push your code to a public GitHub repository.

#### Step 2: Create Release

1. Go to GitHub repository → Releases
2. Click "Create a new release"
3. Tag version: `v1.0.0`
4. Release title: `1.0.0`
5. Publish release

#### Step 3: Configure JitPack

1. Go to [https://jitpack.io](https://jitpack.io)
2. Enter your GitHub repository URL
3. Click "Look up" then "Get it"

#### Step 4: Use in Projects

```kotlin
// In settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// In build.gradle.kts
dependencies {
    implementation("com.github.yourusername:ntfy-android:1.0.0")
}
```

---

### Option 3: Publish to GitHub Packages

#### Step 1: Create GitHub Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate new token with `write:packages` scope

#### Step 2: Update build.gradle.kts

```kotlin
publishing {
    publications {
        create<MavenPublication>("gpr") {
            groupId = "com.purpletear"
            artifactId = "ntfy"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/YOUR_USERNAME/ntfy-android")
            credentials {
                username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

#### Step 3: Publish

```bash
export GITHUB_TOKEN=your_token_here
./gradlew :ntfy:publishGprPublicationToGitHubPackagesRepository
```

---

## License

Apache-2.0
