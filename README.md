# Alpha

Alpha is a command-line task manager for todos, deadlines, and events, with automatic saving and task searches.
See the [User Guide](https://yuncongshen.github.io/ip/) for commands and examples.
The sections below explain project setup, building, and application behavior.

## Setting up in IntelliJ IDEA

Prerequisites: JDK 25 and an up-to-date version of IntelliJ IDEA.

1. Open IntelliJ IDEA (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first).
1. Open the project in IntelliJ IDEA as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/alpha/Alpha.java` file, right-click it, and choose `Run Alpha.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output:
   ```text
         █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗
        ██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
        ███████║██║     ██████╔╝███████║███████║
        ██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
        ██║  ██║███████╗██║     ██║  ██║██║  ██║
        ╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
        Yooo! I'm Alpha. What can I help you with today?
       ____________________________________________________________
   ```

   Alpha then waits for a command. Enter `bye` to exit:

   ```text
        Bye. Hope to see you again soon!
       ____________________________________________________________
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

## Deadline dates

Create a deadline with a full date in `yyyy-MM-dd` format (year-month-day):

```text
deadline return book /by 2019-10-15
```

Alpha stores the deadline as a Java `LocalDateTime`, tracking whether a time was supplied,
and displays this date-only input as:

```text
[D][ ] return book (by: Oct 15 2019)
```

Day-first slash dates are also accepted, with an optional 24-hour time:

```text
deadline return book /by 2/12/2019 1800
deadline return book /by 02/12/2019 18:00
```

Both mean 2 December 2019 at 6pm and display as `[D][ ] return book (by: Dec 02 2019, 18:00)`.
Use one or two digits for the day/month and four digits for the year. Time must be `HHmm` or `HH:mm`;
`0000` means midnight, and `2359` means 11:59pm. Slash dates without a time also work.
Date-only deadlines do not display a time; an explicit midnight deadline displays `00:00`.

Month names are always English. Dates and times are validated strictly: `2024-02-29` is valid,
but `2023-02-29`, `31/4/2019 1800`, and `2/12/2019 2400` are rejected.
Missing dates, natural-language dates, AM/PM times, seconds, and time zones are unsupported.
Rejected commands leave tasks and the save file unchanged. Event time fields remain free text.

The save file retains ISO dates, such as `D | 0 | return book | 2019-10-15`, or ISO datetimes,
such as `D | 0 | return book | 2019-12-02T18:00`. Both reload without depending on the display
format or computer locale; explicit midnight and date-only values remain distinct.
The ISO datetime format is also accepted as input. Existing snapshot formats are still
recognized, but their deadline fields must contain a supported date or datetime. Old values such as `June 6th`
or blank deadline dates cannot be converted reliably: Alpha reports the affected line and exits
without changing the file. With Alpha closed, back up the file and replace those fields with the
intended full dates before restarting. Base64 snapshots need their date field encoded in Base64,
or conversion to the documented readable format. This update does not change existing files automatically.

## Find deadlines on a date

Use `on 2019-12-02` or `on 2/12/2019` to list deadlines on 2 December 2019.
The query accepts a date without a time and includes completed and incomplete deadlines,
whether date-only or timed (including midnight and 23:59). Results keep their original
list order and task numbers, so you can use those numbers with `mark`, `unmark`, or `delete`.
Alpha prints `No deadlines on Dec 02 2019.` when nothing matches.

This command does not change tasks or write the save file. Todos and events are excluded;
events still have free-text time fields. Invalid or impossible query dates produce an error.

## Find tasks by description

Use `find book` to search descriptions across todos, deadlines, and events, including completed tasks.
Matching ignores case and uses literal substrings: `book` also matches `BOOK` and `notebook`.
You can search for a phrase with `find return book`; surrounding spaces are ignored.
Punctuation is literal, not a regular expression. Dates, time fields, and status labels are not searched.

Results retain their original list order and task numbers, just like `on`, so the displayed numbers
work with `mark`, `unmark`, and `delete`. If existing tasks do not match, Alpha prints
`No matching tasks found. Try a shorter keyword or use "list" to see all tasks.`
If the task list is empty, it prints
`Your task list is empty. Add a task first, e.g. "todo read book".`
Missing or blank keywords produce an error. Searching does not modify tasks or the save file.

## Saving and loading tasks (Level-7)

Run Alpha with the project root as its working directory. Successful `todo`, `deadline`, `event`,
`mark`, `unmark`, and `delete` commands automatically replace `data/alpha.txt` with the current task list.
The `data` directory is created on the first save. Listing tasks, invalid commands, and exiting do not write the file.
The application uses the relative path `Path.of("data", "alpha.txt")`; Java supplies the appropriate path separators
for the host OS. Storage keeps this path relative to the working directory, with no machine-specific drive or folder.

New saves use the header `ALPHA-2` and readable UTF-8 text, with ` | ` between fields. Each record contains
the task type (`T`, `D`, or `E`), completion flag (`0` or `1`), description, and any time fields:

```text
ALPHA-2
T | 0 | read book
D | 1 | return book | 2019-06-06
E | 0 | project meeting | Monday 2pm | Monday 4pm
```

Alpha loads this file on startup, restoring task types, descriptions, time fields, order, and completion status.
A missing or empty file starts an empty list. Loading does not rewrite the file; subsequent task changes save the
restored list together with any new tasks. If the file cannot be read or parsed, Alpha reports the problem and exits
without overwriting it. Blank lines, a leading UTF-8 BOM, and Windows/Unix line endings are accepted.
Invalid records report their line number; invalid UTF-8 is rejected. Files larger than 1 MiB are rejected as a
whole. Tasks use an ArrayList, so there is no fixed 100-task limit, including when loading a saved list.

Special characters are escaped: `\|` for a literal pipe, `\\` for a backslash, and `\n`, `\r`, `\t` for
newline, carriage return, and tab. Keep the spaces around field separators when editing a file manually.

Existing Base64 (`ALPHA-1`) and earlier display-format snapshots with valid ISO deadline dates still load
and are converted to readable text
on the next successful task change. Use the rebuilt JAR; older releases cannot read `ALPHA-2` files. If an old
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
important data. The combined UI suite covers commands, persistence, deletion, and collection growth; see its latest session record.
These checks cover saving/loading, absent files and folders, and malformed records with preservation of the original
file. Native Windows/macOS execution and the remaining disk-failure scenarios have not been tested.
