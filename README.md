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
