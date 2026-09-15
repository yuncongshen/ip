# Alpha project template

This is a project template for a greenfield Java project. It is named _Alpha_. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/alpha/Alpha.java` file, right-click it, and choose `Run Alpha.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output:
   ```
    █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗
   ██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
   ███████║██║     ██████╔╝███████║███████║
   ██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
   ██║  ██║███████╗██║     ██║  ██║██║  ██║
   ╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
   Yooo! I'm Alpha. What can I help you with today?

   Bye! See you soon!
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Building and running a fat JAR

Install **JDK 25** and set `JAVA_HOME` to its installation folder, with its `bin` folder on `PATH`.
Check `java -version` reports version 25. In IntelliJ, reload the Gradle project and select JDK 25 as the
Gradle JVM under Settings > Build, Execution, Deployment > Build Tools > Gradle.

The project includes the Gradle 9.6.1 wrapper (`gradlew`, `gradlew.bat`, and `gradle/wrapper/`), so you do not
need to install Gradle separately. The first build needs internet access to download Gradle and the Shadow
plugin. The wrapper verifies the Gradle download against its pinned SHA-256 checksum.

From a terminal in the project root, build the JAR:

```powershell
# Windows PowerShell
.\gradlew.bat shadowJar
```

```sh
# macOS, Linux, or WSL
sh ./gradlew shadowJar
```

On macOS with SDKMAN, select the course JDK first using `sdk use java 25.0.3.fx-zulu` if needed.
You can prepend `clean` to the build command to remove old build output, for example
`.\gradlew.bat clean shadowJar`. The `clean` task does not delete your saved task data.

The output is **`build/libs/alpha.jar`**. Run it from the project root:

```sh
java -jar build/libs/alpha.jar
```

Type commands such as `list`, `todo read book`, and `bye` in that terminal. Launching from the project root
keeps task data at `data/alpha.txt`. Data paths follow your terminal's working directory, not the JAR's location.
To distribute the application, copy `alpha.jar` to another folder or computer and run `java -jar alpha.jar`
from the folder where you want its data stored. The recipient needs Java 25, but does not need Gradle.

`build.gradle` applies the `application` plugin and Shadow 9.5.1 (`com.gradleup.shadow`). It selects the Java 25
toolchain, compiles UTF-8 source, sets `alpha.Alpha` as the executable entry point, and names the fat JAR
`alpha.jar`. Shadow includes application classes and runtime dependencies and merges service-provider files.
There are no external runtime dependencies yet, so the resulting JAR is small. Java itself, saved task data,
and test files are not bundled. `settings.gradle` fixes the project name as `alpha`.

The ordinary thin JAR task is disabled to avoid distributing the wrong artifact. `shadowJar` packages the
application; it does not run the Python UI suite. To run that suite separately with Java 25, follow
[the UI test plan](test/ui-test-plan.md). Build output and Gradle caches are already excluded by `.gitignore`.

Verified with Java 25.0.3 in Linux/WSL: `shadowJar` builds successfully, the manifest names `alpha.Alpha`, and a
copy of the JAR runs `list` and `bye` from a temporary folder without the source tree. The native Windows wrapper
has not been executed in this environment.

References: [Shadow application integration](https://gradleup.com/shadow/application-plugin/) and
[Gradle Java compatibility](https://docs.gradle.org/current/userguide/compatibility.html).

## Saving and loading tasks (Level-7)

Run Alpha with the project root as its working directory. Successful `todo`, `deadline`, `event`,
`mark`, and `unmark` commands automatically replace `data/alpha.txt` with the current task list.
The `data` directory is created on the first save. Listing tasks, invalid commands, and exiting do not write the file.
The application uses the relative path `Path.of("data", "alpha.txt")`; Java supplies the appropriate path separators
for the host OS. Storage keeps this path relative to the working directory, with no machine-specific drive or folder.

The UTF-8 file uses the version header `ALPHA-1` and one task per line. Records use `|` separators, a type
(`T`, `D`, or `E`), a completion flag (`0` or `1`), and Base64-encoded UTF-8 text fields. Encoding keeps
punctuation and line breaks inside a description or time field from being mistaken for record structure.
Base64 is encoding, not encryption. For example, an unfinished `read book` task is stored as:

```text
ALPHA-1
T|0|cmVhZCBib29r
```

Alpha loads this file on startup, restoring task types, descriptions, time fields, order, and completion status.
A missing or empty file starts an empty list. Loading does not rewrite the file; subsequent task changes save the
restored list together with any new tasks. If the file cannot be read or parsed, Alpha reports the problem and exits
without overwriting it. Blank lines, a leading UTF-8 BOM, and Windows/Unix line endings are accepted.
Invalid records report their line number; invalid UTF-8 is rejected. Files larger than 1 MiB or containing more
than 100 tasks are rejected as a whole. The array limit remains until the Collections extension is merged.

Earlier display-format snapshots are still readable and are converted on the next successful save. If an old
record has ambiguous time separators, Alpha rejects it rather than guessing how its fields were divided.

Saving writes and flushes a temporary file in the same directory, then atomically replaces the snapshot.
If atomic replacement is unsupported or a write fails, the requested task change is rolled back and the old
snapshot is retained. Success messages are printed only after saving. Failed temporary-file cleanup reports
the leftover path. A cleanup or lock-close warning after a successful replacement does not undo the saved change.

A `.alpha.lock` file coordinates saves between Alpha sessions; it may remain after exit. Alpha checks that
the snapshot still matches what it last loaded or saved, and rejects conflicting edits instead of overwriting them.
Restart Alpha after an external edit to reload the latest file. Direct symbolic links and directories at the
snapshot path are rejected. Local task data and temporary files are excluded from Git.

These protections depend on the filesystem's locking and atomic-move support. They do not promise recovery
from hardware failure or protect against an unrelated editor racing the final replacement. Keep backups of
important data. Compilation and all 10 UI cases (UI-12 through UI-21) passed with Java 25.0.3 in Linux/WSL.
These checks cover saving/loading, absent files and folders, and malformed records with preservation of the original
file. Native Windows/macOS execution and the remaining disk-failure scenarios have not been tested.
