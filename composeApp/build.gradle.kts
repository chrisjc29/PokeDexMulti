@file:OptIn(ExperimentalRoborazziApi::class)

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.awt.image.BufferedImage
import java.io.File
import java.util.Properties
import javax.imageio.ImageIO

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kover)
}

// Firebase is opt-in via the `firebase.enabled` gradle property (default false). When off, the
// google-services/Crashlytics plugins are NOT applied, no Firebase dependency is pulled, and the
// build needs no config files. The Firebase plugins are still declared `apply false` at the root,
// so they're on the classpath and we just apply them conditionally here.
val firebaseEnabled = providers.gradleProperty("firebase.enabled").orNull?.toBoolean() ?: false

if (firebaseEnabled) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
    apply(plugin = libs.plugins.crashlytics.get().pluginId)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Only iosArm64 (real devices) + iosSimulatorArm64 (Apple-Silicon simulator). Do NOT add
    // iosX64 here: Compose Multiplatform dropped the iosX64 (Intel-Mac simulator) target in stable
    // 1.11.0 — the runtime-iosx64 artifact only exists up to 1.11.0-alpha01 — so listing it against
    // the pinned compose 1.11.0 fails with "Couldn't resolve dependency ... for iosX64".
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Without this the linker warns it cannot infer a bundle ID and falls back to the
            // bundle *name*, which is not a valid identifier for anything that reads it later.
            binaryOption("bundleId", "com.unomaster.pokedexgame")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // The @Preview annotation. NOT compose.components.uiToolingPreview, which supplies the
            // JetBrains annotation deprecated in CMP 1.10.
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Arrow - Either and the Raise DSL. api, not implementation: Either<DomainError, T>
            // appears in the return types of domain interfaces, so it is part of this module's
            // public surface and consumers must see arrow.core on their compile classpath.
            api(libs.arrow.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)

            // Multiplatform ViewModel for MVI
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Image loading — the Pokemon artwork comes from the PokeAPI sprite CDN.
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }

        // Domain / data / state-production unit tests + shared fakes. The Compose UI tiers live in
        // androidUnitTest because they need Robolectric configuration commonTest can't express.
        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
        }

        androidMain {
            // Source-set switching picks which analytics implementation compiles in. Common code
            // only ever sees the Analytics/CrashReporter interfaces, so nothing else changes when
            // the flag flips — see the analytics package for the two variant source sets.
            if (firebaseEnabled) {
                kotlin.srcDir("src/androidFirebase/kotlin")
            } else {
                kotlin.srcDir("src/androidNoFirebase/kotlin")
            }

            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.android)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.compose.ui.tooling.preview)

                // Firebase deps only when enabled (BOM-managed).
                if (firebaseEnabled) {
                    implementation(project.dependencies.platform(libs.firebase.bom))
                    implementation(libs.firebase.analytics)
                    implementation(libs.firebase.crashlytics)
                }
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        // Component + feature + regression tiers all run here (Android JVM via Robolectric).
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.roborazzi)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.preview.scanner)
                implementation(libs.composable.preview.scanner) // scans @Preview composables
                implementation(libs.robolectric)
                implementation(libs.junit)                      // JUnit 4 runtime for generated tests
                implementation(libs.androidx.compose.ui.tooling.preview)
                implementation(libs.androidx.compose.ui.test.junit4.android)
            }
        }
    }
}

// The preview renderer. The annotation alone only marks a composable as previewable - without
// ui-tooling on the debug classpath the IDE has nothing to draw it with, which is why previews
// render blank. Debug-only, so it never ships in a release build.
dependencies {
    debugImplementation(libs.compose.ui.tooling)
    // Supplies the ComponentActivity that runComposeUiTest launches — debug-only, so it never
    // ships in a release build.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Roborazzi auto-generates one Robolectric screenshot test per @Preview under the app package.
// Record goldens: ./gradlew recordRoborazziDebug   Verify: ./gradlew verifyRoborazziDebug
roborazzi {
    // Goldens live in the source tree and are committed. The default is build/outputs/roborazzi,
    // which is generated output and git-ignored — so every machine and every CI run would start with
    // no reference images and "verify" would have nothing to compare against. Comparison diffs still
    // go to build/, since those are throwaway artifacts of a failed run.
    outputDir.set(layout.projectDirectory.dir("src/androidUnitTest/screenshots"))

    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("com.unomaster.pokedexgame")
    }
}

// The README screenshots, cut from the goldens rather than captured by hand.
//
// docs/screenshots used to be five phone screenshots taken off a device, which meant nothing tied
// them to the app: renaming the home title to "Who's That" left home.png showing the old one, and
// no test, no lane and no reviewer would ever have said so. The two screens that a golden can stand
// in for are now generated from the goldens, and `fastlane android ci` regenerates them and fails if
// what comes out differs from what is committed - so those images cannot drift from the UI again.
//
// The other three stay hand-captured, and deliberately. Previews pass an unresolvable artwork URL so
// that no golden depends on the network, which means the game goldens draw the placeholder Pokeball
// exactly where the real screen shows the silhouette and the revealed Pokemon - the two things those
// README images exist to show. Generating them would cost the reader more than staleness does.
val readmeScreenshotsFromGoldens = mapOf(
    "home.png" to
        "com.unomaster.pokedexgame.presentation.home.HomeScreenKt.HomeContentPreview.png",
    "settings.png" to
        "com.unomaster.pokedexgame.presentation.settings.SettingsScreenKt.SettingsContentEnabledPreview.png",
)

// Halved, because a golden is 1078x2337 and the README lays four of them out side by side in a
// table. The factor is exactly two, so each output pixel is the mean of a 2x2 block: no resampling
// kernel to pick, and nothing for the LCD's one-pixel scanlines to alias against.
abstract class DeriveReadmeScreenshots : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val goldens: DirectoryProperty

    /** README file name -> the golden it is cut from. */
    @get:Input
    abstract val sources: MapProperty<String, String>

    @get:Internal
    abstract val destination: DirectoryProperty

    // The individual files, never the directory. docs/screenshots also holds the three
    // hand-captured images, and declaring it as an output directory invites Gradle's stale-output
    // cleanup to delete the files this task does not produce.
    @get:OutputFiles
    abstract val images: ConfigurableFileCollection

    @TaskAction
    fun derive() {
        val goldenDir = goldens.get().asFile
        val outputDir = destination.get().asFile

        sources.get().forEach { (name, golden) ->
            val source = File(goldenDir, golden)
            if (!source.isFile) {
                error(
                    "docs/screenshots/$name is cut from $golden, which does not exist. If the " +
                        "preview was renamed, update readmeScreenshotsFromGoldens in " +
                        "composeApp/build.gradle.kts.",
                )
            }

            val scaled = halve(ImageIO.read(source))
            val target = File(outputDir, name)

            // Rewriting a file whose pixels already match would churn its bytes for nothing - PNG
            // encoders are free to differ between JDKs, and the ci lane decides whether these
            // images are stale by looking at the working tree.
            if (target.isFile && samePixels(ImageIO.read(target), scaled)) return@forEach

            ImageIO.write(scaled, "png", target)
            logger.lifecycle("Wrote docs/screenshots/$name from $golden")
        }
    }

    private fun halve(source: BufferedImage): BufferedImage {
        // Rounded up, so an odd height keeps its last row rather than dropping it. That row averages
        // the one source row it has.
        val width = (source.width + 1) / 2
        val height = (source.height + 1) / 2
        val target = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var alpha = 0
                var red = 0
                var green = 0
                var blue = 0
                var count = 0

                for (dy in 0..1) {
                    for (dx in 0..1) {
                        val sourceX = x * 2 + dx
                        val sourceY = y * 2 + dy
                        if (sourceX >= source.width || sourceY >= source.height) continue

                        val pixel = source.getRGB(sourceX, sourceY)
                        alpha += (pixel ushr 24) and 0xFF
                        red += (pixel ushr 16) and 0xFF
                        green += (pixel ushr 8) and 0xFF
                        blue += pixel and 0xFF
                        count++
                    }
                }

                // Half-up rather than truncating: truncation darkens every image by up to one level
                // per channel, which is invisible on its own and permanent once committed.
                fun mean(sum: Int) = (sum + count / 2) / count

                target.setRGB(
                    x,
                    y,
                    (mean(alpha) shl 24) or (mean(red) shl 16) or (mean(green) shl 8) or mean(blue),
                )
            }
        }

        return target
    }

    private fun samePixels(left: BufferedImage, right: BufferedImage): Boolean {
        if (left.width != right.width || left.height != right.height) return false

        for (y in 0 until left.height) {
            for (x in 0 until left.width) {
                if (left.getRGB(x, y) != right.getRGB(x, y)) return false
            }
        }

        return true
    }
}

val readmeScreenshotDir = File(rootDir, "docs/screenshots")

tasks.register<DeriveReadmeScreenshots>("updateDocsScreenshots") {
    group = "documentation"
    description = "Regenerate the README screenshots that are cut from the Roborazzi goldens."

    goldens.set(layout.projectDirectory.dir("src/androidUnitTest/screenshots"))
    sources.set(readmeScreenshotsFromGoldens)
    destination.set(readmeScreenshotDir)
    images.setFrom(readmeScreenshotsFromGoldens.keys.map { File(readmeScreenshotDir, it) })
}

// Coverage is measured where bugs live: domain rules, data mapping and state production. Compose
// screens are covered by the component/feature/Roborazzi tiers rather than line counting, and
// platform code cannot execute in a JVM unit test - both are excluded so the number stays honest.
kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.unomaster.pokedexgame.di",
                    "com.unomaster.pokedexgame.navigation",
                    "com.unomaster.pokedexgame.presentation.theme",
                    "com.unomaster.pokedexgame.presentation.common",
                    // Platform seams: the analytics implementations and the clock actual exist per
                    // platform and only one variant is even on this classpath, so counting their
                    // lines measures which source set compiled, not whether anything is tested.
                    "com.unomaster.pokedexgame.analytics",
                    "com.unomaster.pokedexgame.time",
                )
                classes(
                    "com.unomaster.pokedexgame.*Screen*",
                    "com.unomaster.pokedexgame.*Kt\\$*",
                    "com.unomaster.pokedexgame.ComposableSingletons*",
                    // Android entry points and the SharedPreferences-backed store: framework
                    // plumbing with no logic, and nothing a JVM unit test can meaningfully drive.
                    "com.unomaster.pokedexgame.MainActivity",
                    "com.unomaster.pokedexgame.MainApplication",
                    "com.unomaster.pokedexgame.data.local.AndroidKeyValueStore",
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule("Logic layers stay covered") { minBound(90) }
        }
    }
}

// Release signing is read from keystore.properties (local dev) or environment variables (CI).
// If neither is present the release build is left unsigned, so debug builds, tests and CI jobs that
// don't need a signed artifact keep working without any secrets.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
// Blank counts as absent, not as a value.
//
// Both sources hand back empty strings rather than nulls when nothing is configured: a GitHub
// Actions `env:` entry whose expression resolved to nothing is set to "", and
// keystore.properties.PLACEHOLDER ships with `storeFile=` on purpose. Either one made
// hasReleaseSigning true, and `file("")` resolves to the project directory - so the release variant
// was configured to sign itself with a directory, and failed at packaging with a message about the
// keystore rather than about the missing configuration.
fun signingValue(propKey: String, envKey: String): String? =
    (keystoreProperties.getProperty(propKey) ?: System.getenv(envKey))?.takeIf { it.isNotBlank() }
val hasReleaseSigning = signingValue("storeFile", "ANDROID_KEYSTORE_FILE") != null

android {
    namespace = "com.unomaster.pokedexgame"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.unomaster.pokedexgame"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        // CI passes -PversionCode/-PversionName (a Fastlane lane feeds it the run number) so every
        // distributed build is distinguishable; local builds get 1 / 1.0.
        versionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull() ?: 1
        versionName = providers.gradleProperty("versionName").orNull ?: "1.0"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(signingValue("storeFile", "ANDROID_KEYSTORE_FILE")!!)
                storePassword = signingValue("storePassword", "ANDROID_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "ANDROID_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        // Robolectric needs Android resources available to unit tests.
        unitTests.isIncludeAndroidResources = true
        // Compose draws through Skia; without the hardware render mode, screenshots come out empty.
        unitTests.all { it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware" }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
}

// The Compose UI tiers only run on debug: ui-test-manifest — which supplies the ComponentActivity
// runComposeUiTest launches — is a debugImplementation, and it stays that way because it injects an
// activity into the merged manifest and has no business in a release build. Leaving the release unit
// test variant enabled just means `allTests` runs the same tests a second time against a manifest
// that cannot host them, and every UI test fails with "Unable to resolve activity for Intent".
androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        variant.enableUnitTest = false
    }
}
