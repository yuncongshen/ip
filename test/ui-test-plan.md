# UI Test Plan

This plan combines the independent Level-6 and Level-7 suites on master.

## Test environment and execution

- Use Java 25 and Python 3. Run: `python3 test/run-ui-tests.py --java-home /path/to/jdk-25`.
- Compile all `src/main/java` files with UTF-8 encoding into a fresh temporary output directory.
- Start `alpha.Alpha` in a separate temporary working directory per case. Never use real project data for tests.
- Use UTF-8 stdin/stdout and Java's `-Dfile.encoding=UTF-8` option.
- Send each Inputs line literally with a final newline, including blank lines, then close stdin after the last input.
- Wait for each response divider and verify its file checkpoint before sending the next command. This proves saving happens while the app is running, not just at exit.
- Capture stdout, stderr, exit code, and timeout status. Timeout: 10 seconds per case, including checkpoint waits.
- Compare complete stdout and file contents after normalizing CRLF to LF only. Preserve spaces, blank lines, and final newlines.
- The banner's first line intentionally has a trailing space. Expected stdout includes the banner and dividers but excludes echoed input.
- Stop at the first compilation, launch, timeout, stderr, nonzero exit, output mismatch, or file-check failure. Mark later cases NOT RUN.
- On failure, retain complete actual and expected output, the first differing line if available, and the checkpoint error.
- A session passes only if every case passes. Record Java version, commit, working-tree state, time with timezone, commands, and per-case results.
- The console record uses INPUT / OUTPUT / RESULT sections and is retained under Latest test session.

## File checkpoint format

Each case supplies an aim, exact input, complete expected stdout, and JSON file checkpoints.
All files refer to `data/alpha.txt` under that case's temporary working directory.
`initialFile: null` means no snapshot initially; a string seeds an existing UTF-8 file. `initialDirectory: true` creates an empty data folder first.
`afterInput` is a one-based input line number. `contents: null` means the file is absent. The data directory must also be absent unless `directoryExists: true` is specified.
`unchanged: true` also requires the file's modification timestamp to remain unchanged from the previous checkpoint.
The runner checks initial file state after startup, every command checkpoint, and file state again after exit.
An empty Inputs block sends zero bytes and then EOF; a blank line inside it instead sends an empty command.
JSON `\n` represents a literal newline in the expected file. It is not text stored on disk.

## Scope

The happy path creates the data directory and rewrites a versioned snapshot after successful add, mark, and unmark commands.
Deletion now saves automatically and rolls back if saving fails. Save failures now roll back the command; atomic replacement and external-edit checks protect existing snapshots.
UI-14 covers restoring and updating existing tasks; UI-15 covers an empty file. UI-12 and UI-13 also cover startup without a save file.
UI-14 covers legacy import and conversion; UI-16 covers the ALPHA-1 format. UI-17 adds the missing-file/existing-folder case. UI-18 through UI-21 cover corrupted snapshots and verify the original data survives.

Pending error-path coverage: malformed UTF-8/Base64, empty descriptions, unknown versions, ambiguous legacy fields, BOM/blank lines/CRLF, the 1 MiB boundary, permission denial, full disks, directories or symlinks at the save path, external edits, overlapping saves, unsupported atomic moves, rollback, and temporary-file cleanup.
These scenarios are a coverage backlog, not executed tests or claims of verified behavior.

Console-only cases UI-01 through UI-11 send their full input at once and compare stdout.
They use fresh empty working directories. Cases with File checkpoints also inspect disk state after every command.
UI-06 uses the integrated task-number errors for bare mark/unmark commands.

## Test cases

### UI-01: Startup, empty list, and exit

**Aim:** Verify the greeting, an empty list, and that bye stops processing later input.

**Inputs:**

```text
list
bye
todo ignored
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-02: End of input without bye

**Aim:** Verify clean EOF termination without an unsolicited farewell.

**Inputs:**

```text
list
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
```

### UI-03: Add all task types

**Aim:** Verify descriptions, time fields, insertion order, and counts through list.

**Inputs:**

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-04: Mark, repeat, and unmark

**Aim:** Verify repeated status changes are harmless and list reflects the stored status.

**Inputs:**

```text
todo read book
mark 1
mark 1
list
unmark 1
unmark 1
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-05: Delete middle, last, first, and only task

**Aim:** Verify removal, renumbering, preserved status, emptying the list, and adding again.

**Inputs:**

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo borrow book
mark 3
delete 2
list
delete 3
list
delete 1
list
delete 1
list
todo replacement
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
     3.[T][ ] borrow book
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] borrow book
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [E][X] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 0 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] replacement
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] replacement
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-06: Invalid task numbers and recovery

**Aim:** Reject missing, malformed, nonpositive, overflowing, and out-of-range indices without changing tasks.

**Inputs:**

```text
todo read book
mark
list
mark 
list
mark abc
list
mark 0
list
mark -1
list
mark 2
list
mark 999999999999
list
mark 1 2
list
unmark
list
unmark 
list
unmark abc
list
unmark 0
list
unmark -1
list
unmark 2
list
unmark 999999999999
list
unmark 1 2
list
delete
list
delete 
list
delete abc
list
delete 0
list
delete -1
list
delete 2
list
delete 999999999999
list
delete 1 2
list
mark 1
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-07: Empty-list task operations

**Aim:** Reject mark, unmark, and delete on an empty list and remain usable.

**Inputs:**

```text
mark 1
unmark 1
delete 1
list
todo read book
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-08: Invalid commands and empty descriptions

**Aim:** Verify blank and unknown commands and empty descriptions preserve existing tasks; valid input still works.

**Inputs:**

```text
todo read book

dance
todo
todo   
deadline
event
list
todo next
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] next
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] next
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-09: Whitespace compatibility

**Aim:** Record existing whitespace behavior explicitly: trim task bodies and numeric arguments, but match command names exactly.

**Inputs:**

```text
todo   read book   
mark   1   
list
 list
list 
LIST
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-10: Missing time markers compatibility

**Aim:** Record existing acceptance of descriptions without time markers; this is a compatibility baseline, not a new validation requirement.

**Inputs:**

```text
deadline return book
event meeting
event lunch /from noon
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: )
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] meeting (from:  to: )
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] lunch (from: noon to: )
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] return book (by: )
     2.[E][ ] meeting (from:  to: )
     3.[E][ ] lunch (from: noon to: )
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-11: Grow beyond 100 tasks

**Aim:** Verify A-Collections removes the former capacity limit, keeps every task, and supports deletion and addition after growth.

**Inputs:**

```text
todo task 1
todo task 2
todo task 3
todo task 4
todo task 5
todo task 6
todo task 7
todo task 8
todo task 9
todo task 10
todo task 11
todo task 12
todo task 13
todo task 14
todo task 15
todo task 16
todo task 17
todo task 18
todo task 19
todo task 20
todo task 21
todo task 22
todo task 23
todo task 24
todo task 25
todo task 26
todo task 27
todo task 28
todo task 29
todo task 30
todo task 31
todo task 32
todo task 33
todo task 34
todo task 35
todo task 36
todo task 37
todo task 38
todo task 39
todo task 40
todo task 41
todo task 42
todo task 43
todo task 44
todo task 45
todo task 46
todo task 47
todo task 48
todo task 49
todo task 50
todo task 51
todo task 52
todo task 53
todo task 54
todo task 55
todo task 56
todo task 57
todo task 58
todo task 59
todo task 60
todo task 61
todo task 62
todo task 63
todo task 64
todo task 65
todo task 66
todo task 67
todo task 68
todo task 69
todo task 70
todo task 71
todo task 72
todo task 73
todo task 74
todo task 75
todo task 76
todo task 77
todo task 78
todo task 79
todo task 80
todo task 81
todo task 82
todo task 83
todo task 84
todo task 85
todo task 86
todo task 87
todo task 88
todo task 89
todo task 90
todo task 91
todo task 92
todo task 93
todo task 94
todo task 95
todo task 96
todo task 97
todo task 98
todo task 99
todo task 100
todo task 101
list
delete 100
todo replacement
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 1
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 2
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 3
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 4
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 5
     Now you have 5 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 6
     Now you have 6 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 7
     Now you have 7 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 8
     Now you have 8 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 9
     Now you have 9 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 10
     Now you have 10 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 11
     Now you have 11 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 12
     Now you have 12 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 13
     Now you have 13 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 14
     Now you have 14 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 15
     Now you have 15 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 16
     Now you have 16 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 17
     Now you have 17 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 18
     Now you have 18 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 19
     Now you have 19 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 20
     Now you have 20 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 21
     Now you have 21 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 22
     Now you have 22 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 23
     Now you have 23 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 24
     Now you have 24 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 25
     Now you have 25 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 26
     Now you have 26 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 27
     Now you have 27 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 28
     Now you have 28 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 29
     Now you have 29 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 30
     Now you have 30 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 31
     Now you have 31 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 32
     Now you have 32 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 33
     Now you have 33 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 34
     Now you have 34 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 35
     Now you have 35 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 36
     Now you have 36 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 37
     Now you have 37 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 38
     Now you have 38 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 39
     Now you have 39 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 40
     Now you have 40 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 41
     Now you have 41 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 42
     Now you have 42 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 43
     Now you have 43 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 44
     Now you have 44 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 45
     Now you have 45 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 46
     Now you have 46 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 47
     Now you have 47 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 48
     Now you have 48 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 49
     Now you have 49 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 50
     Now you have 50 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 51
     Now you have 51 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 52
     Now you have 52 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 53
     Now you have 53 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 54
     Now you have 54 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 55
     Now you have 55 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 56
     Now you have 56 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 57
     Now you have 57 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 58
     Now you have 58 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 59
     Now you have 59 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 60
     Now you have 60 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 61
     Now you have 61 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 62
     Now you have 62 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 63
     Now you have 63 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 64
     Now you have 64 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 65
     Now you have 65 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 66
     Now you have 66 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 67
     Now you have 67 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 68
     Now you have 68 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 69
     Now you have 69 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 70
     Now you have 70 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 71
     Now you have 71 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 72
     Now you have 72 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 73
     Now you have 73 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 74
     Now you have 74 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 75
     Now you have 75 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 76
     Now you have 76 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 77
     Now you have 77 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 78
     Now you have 78 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 79
     Now you have 79 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 80
     Now you have 80 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 81
     Now you have 81 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 82
     Now you have 82 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 83
     Now you have 83 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 84
     Now you have 84 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 85
     Now you have 85 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 86
     Now you have 86 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 87
     Now you have 87 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 88
     Now you have 88 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 89
     Now you have 89 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 90
     Now you have 90 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 91
     Now you have 91 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 92
     Now you have 92 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 93
     Now you have 93 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 94
     Now you have 94 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 95
     Now you have 95 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 96
     Now you have 96 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 97
     Now you have 97 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 98
     Now you have 98 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 99
     Now you have 99 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 100
     Now you have 100 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 101
     Now you have 101 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 100
     101.[T][ ] task 101
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] task 100
     Now you have 100 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] replacement
     Now you have 101 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 101
     101.[T][ ] replacement
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### UI-12: Save after every task change

**Aim:** Create the data directory on the first add; save all three task types and both status transitions before the next command or exit.

**Inputs:**

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
mark 1
unmark 1
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": null,
  "steps": [
    {
      "afterInput": 1,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n"
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\n"
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 4,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 5,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 6,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n",
      "unchanged": true
    },
    {
      "afterInput": 7,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n",
      "unchanged": true
    }
  ]
}
```

### UI-13: Read-only commands and rejected changes do not create data

**Aim:** Verify listing, invalid input, and exit leave the initially absent data directory untouched.

**Inputs:**

```text
list
todo
mark 1
unknown
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": null,
  "steps": [
    {
      "afterInput": 1,
      "contents": null,
      "unchanged": true
    },
    {
      "afterInput": 2,
      "contents": null,
      "unchanged": true
    },
    {
      "afterInput": 3,
      "contents": null,
      "unchanged": true
    },
    {
      "afterInput": 4,
      "contents": null,
      "unchanged": true
    },
    {
      "afterInput": 5,
      "contents": null,
      "unchanged": true
    }
  ]
}
```

### UI-14: Restore all task types and update the saved list

**Aim:** Load the existing UTF-8 snapshot with order, status, and time fields intact; update restored tasks and append a Unicode description without discarding existing tasks.

**Inputs:**

```text
list
unmark 3
mark 2
todo café
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: June 6th)
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] café
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     4.[T][ ] café
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "[T][X] read book\n[D][ ] return book (by: June 6th)\n[E][X] project meeting (from: Aug 6th 2pm to: 4pm)\n",
  "steps": [
    {
      "afterInput": 1,
      "contents": "[T][X] read book\n[D][ ] return book (by: June 6th)\n[E][X] project meeting (from: Aug 6th 2pm to: 4pm)\n",
      "unchanged": true
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 4,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n"
    },
    {
      "afterInput": 5,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n",
      "unchanged": true
    },
    {
      "afterInput": 6,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n",
      "unchanged": true
    }
  ]
}
```

### UI-15: Load an empty save file

**Aim:** Treat an existing empty file as an empty task list and save a new task normally.

**Inputs:**

```text
list
todo read book
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "",
  "steps": [
    {
      "afterInput": 1,
      "contents": "",
      "unchanged": true
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n"
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n",
      "unchanged": true
    },
    {
      "afterInput": 4,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n",
      "unchanged": true
    }
  ]
}
```

### UI-16: Restore the versioned snapshot and update it

**Aim:** Load the existing UTF-8 snapshot with order, status, and time fields intact; update restored tasks and append a Unicode description without discarding existing tasks.

**Inputs:**

```text
list
unmark 3
mark 2
todo café
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: June 6th)
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] café
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     4.[T][ ] café
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|1|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n",
  "steps": [
    {
      "afterInput": 1,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|1|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n",
      "unchanged": true
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\n"
    },
    {
      "afterInput": 4,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n"
    },
    {
      "afterInput": 5,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n",
      "unchanged": true
    },
    {
      "afterInput": 6,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nD|1|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|cHJvamVjdCBtZWV0aW5n|QXVnIDZ0aCAycG0=|NHBt\nT|0|Y2Fmw6k=\n",
      "unchanged": true
    }
  ]
}
```

### UI-17: First run with an existing data folder

**Aim:** Handle a missing file when its folder already exists; list stays empty and adding creates the snapshot inside the current working directory.

**Inputs:**

```text
list
todo read book
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialDirectory": true,
  "initialFile": null,
  "steps": [
    {
      "afterInput": 1,
      "contents": null,
      "directoryExists": true,
      "unchanged": true
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n"
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|0|cmVhZCBib29r\n",
      "unchanged": true
    }
  ]
}
```

### UI-18: Reject corrupted completion status

**Aim:** Report a line-specific load error and exit without rewriting any of the original file. Empty stdin means immediate EOF, not an empty command.

**Inputs:**

```text
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Invalid field count or completion status. The file has not been changed.
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nT|9|cmVhZCBib29r\n",
  "steps": []
}
```

### UI-19: Reject an incomplete event record

**Aim:** Report a line-specific load error and exit without rewriting any of the original file. Empty stdin means immediate EOF, not an empty command.

**Inputs:**

```text
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown task type or incorrect field count. The file has not been changed.
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nE|0|bWVldGluZw==|TW9u\n",
  "steps": []
}
```

### UI-20: Reject an unknown task type

**Aim:** Report a line-specific load error and exit without rewriting any of the original file. Empty stdin means immediate EOF, not an empty command.

**Inputs:**

```text
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown task type or incorrect field count. The file has not been changed.
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nZ|0|cmVhZCBib29r\n",
  "steps": []
}
```

### UI-21: Preserve a partially valid legacy snapshot

**Aim:** Report a line-specific load error and exit without rewriting any of the original file. Empty stdin means immediate EOF, not an empty command.

**Inputs:**

```text
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown file version or invalid legacy record. The file has not been changed.
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "[T][ ] read book\nnot a task\n",
  "steps": []
}
```

### UI-22: Persist deletion of restored tasks

**Aim:** Delete restored tasks in order, preserve status and numbering, and persist an empty list after the final deletion.

**Inputs:**

```text
delete 2
list
delete 1
delete 1
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[E][ ] meeting (from: Monday to: Tuesday)
    ____________________________________________________________
     Noted. I've removed this task:
       [T][X] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Noted. I've removed this task:
       [E][ ] meeting (from: Monday to: Tuesday)
     Now you have 0 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nT|1|cmVhZCBib29r\nD|0|cmV0dXJuIGJvb2s=|SnVuZSA2dGg=\nE|0|bWVldGluZw==|TW9uZGF5|VHVlc2RheQ==\n",
  "steps": [
    {
      "afterInput": 1,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nE|0|bWVldGluZw==|TW9uZGF5|VHVlc2RheQ==\n"
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|1|cmVhZCBib29r\nE|0|bWVldGluZw==|TW9uZGF5|VHVlc2RheQ==\n",
      "unchanged": true
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nE|0|bWVldGluZw==|TW9uZGF5|VHVlc2RheQ==\n"
    },
    {
      "afterInput": 4,
      "contents": "ALPHA-1\n"
    },
    {
      "afterInput": 5,
      "contents": "ALPHA-1\n",
      "unchanged": true
    },
    {
      "afterInput": 6,
      "contents": "ALPHA-1\n",
      "unchanged": true
    }
  ]
}
```

### UI-23: Restore more than 100 tasks

**Aim:** Verify storage uses the dynamically sized collection after restart, preserving the 101st task and allowing it to be updated.

**Inputs:**

```text
mark 101
list
bye
```

**Expected output:**

```text
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] task 101
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 100
     101.[T][X] task 101
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

**File checkpoints:**

```json
{
  "initialFile": "ALPHA-1\nT|0|dGFzayAx\nT|0|dGFzayAy\nT|0|dGFzayAz\nT|0|dGFzayA0\nT|0|dGFzayA1\nT|0|dGFzayA2\nT|0|dGFzayA3\nT|0|dGFzayA4\nT|0|dGFzayA5\nT|0|dGFzayAxMA==\nT|0|dGFzayAxMQ==\nT|0|dGFzayAxMg==\nT|0|dGFzayAxMw==\nT|0|dGFzayAxNA==\nT|0|dGFzayAxNQ==\nT|0|dGFzayAxNg==\nT|0|dGFzayAxNw==\nT|0|dGFzayAxOA==\nT|0|dGFzayAxOQ==\nT|0|dGFzayAyMA==\nT|0|dGFzayAyMQ==\nT|0|dGFzayAyMg==\nT|0|dGFzayAyMw==\nT|0|dGFzayAyNA==\nT|0|dGFzayAyNQ==\nT|0|dGFzayAyNg==\nT|0|dGFzayAyNw==\nT|0|dGFzayAyOA==\nT|0|dGFzayAyOQ==\nT|0|dGFzayAzMA==\nT|0|dGFzayAzMQ==\nT|0|dGFzayAzMg==\nT|0|dGFzayAzMw==\nT|0|dGFzayAzNA==\nT|0|dGFzayAzNQ==\nT|0|dGFzayAzNg==\nT|0|dGFzayAzNw==\nT|0|dGFzayAzOA==\nT|0|dGFzayAzOQ==\nT|0|dGFzayA0MA==\nT|0|dGFzayA0MQ==\nT|0|dGFzayA0Mg==\nT|0|dGFzayA0Mw==\nT|0|dGFzayA0NA==\nT|0|dGFzayA0NQ==\nT|0|dGFzayA0Ng==\nT|0|dGFzayA0Nw==\nT|0|dGFzayA0OA==\nT|0|dGFzayA0OQ==\nT|0|dGFzayA1MA==\nT|0|dGFzayA1MQ==\nT|0|dGFzayA1Mg==\nT|0|dGFzayA1Mw==\nT|0|dGFzayA1NA==\nT|0|dGFzayA1NQ==\nT|0|dGFzayA1Ng==\nT|0|dGFzayA1Nw==\nT|0|dGFzayA1OA==\nT|0|dGFzayA1OQ==\nT|0|dGFzayA2MA==\nT|0|dGFzayA2MQ==\nT|0|dGFzayA2Mg==\nT|0|dGFzayA2Mw==\nT|0|dGFzayA2NA==\nT|0|dGFzayA2NQ==\nT|0|dGFzayA2Ng==\nT|0|dGFzayA2Nw==\nT|0|dGFzayA2OA==\nT|0|dGFzayA2OQ==\nT|0|dGFzayA3MA==\nT|0|dGFzayA3MQ==\nT|0|dGFzayA3Mg==\nT|0|dGFzayA3Mw==\nT|0|dGFzayA3NA==\nT|0|dGFzayA3NQ==\nT|0|dGFzayA3Ng==\nT|0|dGFzayA3Nw==\nT|0|dGFzayA3OA==\nT|0|dGFzayA3OQ==\nT|0|dGFzayA4MA==\nT|0|dGFzayA4MQ==\nT|0|dGFzayA4Mg==\nT|0|dGFzayA4Mw==\nT|0|dGFzayA4NA==\nT|0|dGFzayA4NQ==\nT|0|dGFzayA4Ng==\nT|0|dGFzayA4Nw==\nT|0|dGFzayA4OA==\nT|0|dGFzayA4OQ==\nT|0|dGFzayA5MA==\nT|0|dGFzayA5MQ==\nT|0|dGFzayA5Mg==\nT|0|dGFzayA5Mw==\nT|0|dGFzayA5NA==\nT|0|dGFzayA5NQ==\nT|0|dGFzayA5Ng==\nT|0|dGFzayA5Nw==\nT|0|dGFzayA5OA==\nT|0|dGFzayA5OQ==\nT|0|dGFzayAxMDA=\nT|0|dGFzayAxMDE=\n",
  "steps": [
    {
      "afterInput": 1,
      "contents": "ALPHA-1\nT|0|dGFzayAx\nT|0|dGFzayAy\nT|0|dGFzayAz\nT|0|dGFzayA0\nT|0|dGFzayA1\nT|0|dGFzayA2\nT|0|dGFzayA3\nT|0|dGFzayA4\nT|0|dGFzayA5\nT|0|dGFzayAxMA==\nT|0|dGFzayAxMQ==\nT|0|dGFzayAxMg==\nT|0|dGFzayAxMw==\nT|0|dGFzayAxNA==\nT|0|dGFzayAxNQ==\nT|0|dGFzayAxNg==\nT|0|dGFzayAxNw==\nT|0|dGFzayAxOA==\nT|0|dGFzayAxOQ==\nT|0|dGFzayAyMA==\nT|0|dGFzayAyMQ==\nT|0|dGFzayAyMg==\nT|0|dGFzayAyMw==\nT|0|dGFzayAyNA==\nT|0|dGFzayAyNQ==\nT|0|dGFzayAyNg==\nT|0|dGFzayAyNw==\nT|0|dGFzayAyOA==\nT|0|dGFzayAyOQ==\nT|0|dGFzayAzMA==\nT|0|dGFzayAzMQ==\nT|0|dGFzayAzMg==\nT|0|dGFzayAzMw==\nT|0|dGFzayAzNA==\nT|0|dGFzayAzNQ==\nT|0|dGFzayAzNg==\nT|0|dGFzayAzNw==\nT|0|dGFzayAzOA==\nT|0|dGFzayAzOQ==\nT|0|dGFzayA0MA==\nT|0|dGFzayA0MQ==\nT|0|dGFzayA0Mg==\nT|0|dGFzayA0Mw==\nT|0|dGFzayA0NA==\nT|0|dGFzayA0NQ==\nT|0|dGFzayA0Ng==\nT|0|dGFzayA0Nw==\nT|0|dGFzayA0OA==\nT|0|dGFzayA0OQ==\nT|0|dGFzayA1MA==\nT|0|dGFzayA1MQ==\nT|0|dGFzayA1Mg==\nT|0|dGFzayA1Mw==\nT|0|dGFzayA1NA==\nT|0|dGFzayA1NQ==\nT|0|dGFzayA1Ng==\nT|0|dGFzayA1Nw==\nT|0|dGFzayA1OA==\nT|0|dGFzayA1OQ==\nT|0|dGFzayA2MA==\nT|0|dGFzayA2MQ==\nT|0|dGFzayA2Mg==\nT|0|dGFzayA2Mw==\nT|0|dGFzayA2NA==\nT|0|dGFzayA2NQ==\nT|0|dGFzayA2Ng==\nT|0|dGFzayA2Nw==\nT|0|dGFzayA2OA==\nT|0|dGFzayA2OQ==\nT|0|dGFzayA3MA==\nT|0|dGFzayA3MQ==\nT|0|dGFzayA3Mg==\nT|0|dGFzayA3Mw==\nT|0|dGFzayA3NA==\nT|0|dGFzayA3NQ==\nT|0|dGFzayA3Ng==\nT|0|dGFzayA3Nw==\nT|0|dGFzayA3OA==\nT|0|dGFzayA3OQ==\nT|0|dGFzayA4MA==\nT|0|dGFzayA4MQ==\nT|0|dGFzayA4Mg==\nT|0|dGFzayA4Mw==\nT|0|dGFzayA4NA==\nT|0|dGFzayA4NQ==\nT|0|dGFzayA4Ng==\nT|0|dGFzayA4Nw==\nT|0|dGFzayA4OA==\nT|0|dGFzayA4OQ==\nT|0|dGFzayA5MA==\nT|0|dGFzayA5MQ==\nT|0|dGFzayA5Mg==\nT|0|dGFzayA5Mw==\nT|0|dGFzayA5NA==\nT|0|dGFzayA5NQ==\nT|0|dGFzayA5Ng==\nT|0|dGFzayA5Nw==\nT|0|dGFzayA5OA==\nT|0|dGFzayA5OQ==\nT|0|dGFzayAxMDA=\nT|1|dGFzayAxMDE=\n"
    },
    {
      "afterInput": 2,
      "contents": "ALPHA-1\nT|0|dGFzayAx\nT|0|dGFzayAy\nT|0|dGFzayAz\nT|0|dGFzayA0\nT|0|dGFzayA1\nT|0|dGFzayA2\nT|0|dGFzayA3\nT|0|dGFzayA4\nT|0|dGFzayA5\nT|0|dGFzayAxMA==\nT|0|dGFzayAxMQ==\nT|0|dGFzayAxMg==\nT|0|dGFzayAxMw==\nT|0|dGFzayAxNA==\nT|0|dGFzayAxNQ==\nT|0|dGFzayAxNg==\nT|0|dGFzayAxNw==\nT|0|dGFzayAxOA==\nT|0|dGFzayAxOQ==\nT|0|dGFzayAyMA==\nT|0|dGFzayAyMQ==\nT|0|dGFzayAyMg==\nT|0|dGFzayAyMw==\nT|0|dGFzayAyNA==\nT|0|dGFzayAyNQ==\nT|0|dGFzayAyNg==\nT|0|dGFzayAyNw==\nT|0|dGFzayAyOA==\nT|0|dGFzayAyOQ==\nT|0|dGFzayAzMA==\nT|0|dGFzayAzMQ==\nT|0|dGFzayAzMg==\nT|0|dGFzayAzMw==\nT|0|dGFzayAzNA==\nT|0|dGFzayAzNQ==\nT|0|dGFzayAzNg==\nT|0|dGFzayAzNw==\nT|0|dGFzayAzOA==\nT|0|dGFzayAzOQ==\nT|0|dGFzayA0MA==\nT|0|dGFzayA0MQ==\nT|0|dGFzayA0Mg==\nT|0|dGFzayA0Mw==\nT|0|dGFzayA0NA==\nT|0|dGFzayA0NQ==\nT|0|dGFzayA0Ng==\nT|0|dGFzayA0Nw==\nT|0|dGFzayA0OA==\nT|0|dGFzayA0OQ==\nT|0|dGFzayA1MA==\nT|0|dGFzayA1MQ==\nT|0|dGFzayA1Mg==\nT|0|dGFzayA1Mw==\nT|0|dGFzayA1NA==\nT|0|dGFzayA1NQ==\nT|0|dGFzayA1Ng==\nT|0|dGFzayA1Nw==\nT|0|dGFzayA1OA==\nT|0|dGFzayA1OQ==\nT|0|dGFzayA2MA==\nT|0|dGFzayA2MQ==\nT|0|dGFzayA2Mg==\nT|0|dGFzayA2Mw==\nT|0|dGFzayA2NA==\nT|0|dGFzayA2NQ==\nT|0|dGFzayA2Ng==\nT|0|dGFzayA2Nw==\nT|0|dGFzayA2OA==\nT|0|dGFzayA2OQ==\nT|0|dGFzayA3MA==\nT|0|dGFzayA3MQ==\nT|0|dGFzayA3Mg==\nT|0|dGFzayA3Mw==\nT|0|dGFzayA3NA==\nT|0|dGFzayA3NQ==\nT|0|dGFzayA3Ng==\nT|0|dGFzayA3Nw==\nT|0|dGFzayA3OA==\nT|0|dGFzayA3OQ==\nT|0|dGFzayA4MA==\nT|0|dGFzayA4MQ==\nT|0|dGFzayA4Mg==\nT|0|dGFzayA4Mw==\nT|0|dGFzayA4NA==\nT|0|dGFzayA4NQ==\nT|0|dGFzayA4Ng==\nT|0|dGFzayA4Nw==\nT|0|dGFzayA4OA==\nT|0|dGFzayA4OQ==\nT|0|dGFzayA5MA==\nT|0|dGFzayA5MQ==\nT|0|dGFzayA5Mg==\nT|0|dGFzayA5Mw==\nT|0|dGFzayA5NA==\nT|0|dGFzayA5NQ==\nT|0|dGFzayA5Ng==\nT|0|dGFzayA5Nw==\nT|0|dGFzayA5OA==\nT|0|dGFzayA5OQ==\nT|0|dGFzayAxMDA=\nT|1|dGFzayAxMDE=\n",
      "unchanged": true
    },
    {
      "afterInput": 3,
      "contents": "ALPHA-1\nT|0|dGFzayAx\nT|0|dGFzayAy\nT|0|dGFzayAz\nT|0|dGFzayA0\nT|0|dGFzayA1\nT|0|dGFzayA2\nT|0|dGFzayA3\nT|0|dGFzayA4\nT|0|dGFzayA5\nT|0|dGFzayAxMA==\nT|0|dGFzayAxMQ==\nT|0|dGFzayAxMg==\nT|0|dGFzayAxMw==\nT|0|dGFzayAxNA==\nT|0|dGFzayAxNQ==\nT|0|dGFzayAxNg==\nT|0|dGFzayAxNw==\nT|0|dGFzayAxOA==\nT|0|dGFzayAxOQ==\nT|0|dGFzayAyMA==\nT|0|dGFzayAyMQ==\nT|0|dGFzayAyMg==\nT|0|dGFzayAyMw==\nT|0|dGFzayAyNA==\nT|0|dGFzayAyNQ==\nT|0|dGFzayAyNg==\nT|0|dGFzayAyNw==\nT|0|dGFzayAyOA==\nT|0|dGFzayAyOQ==\nT|0|dGFzayAzMA==\nT|0|dGFzayAzMQ==\nT|0|dGFzayAzMg==\nT|0|dGFzayAzMw==\nT|0|dGFzayAzNA==\nT|0|dGFzayAzNQ==\nT|0|dGFzayAzNg==\nT|0|dGFzayAzNw==\nT|0|dGFzayAzOA==\nT|0|dGFzayAzOQ==\nT|0|dGFzayA0MA==\nT|0|dGFzayA0MQ==\nT|0|dGFzayA0Mg==\nT|0|dGFzayA0Mw==\nT|0|dGFzayA0NA==\nT|0|dGFzayA0NQ==\nT|0|dGFzayA0Ng==\nT|0|dGFzayA0Nw==\nT|0|dGFzayA0OA==\nT|0|dGFzayA0OQ==\nT|0|dGFzayA1MA==\nT|0|dGFzayA1MQ==\nT|0|dGFzayA1Mg==\nT|0|dGFzayA1Mw==\nT|0|dGFzayA1NA==\nT|0|dGFzayA1NQ==\nT|0|dGFzayA1Ng==\nT|0|dGFzayA1Nw==\nT|0|dGFzayA1OA==\nT|0|dGFzayA1OQ==\nT|0|dGFzayA2MA==\nT|0|dGFzayA2MQ==\nT|0|dGFzayA2Mg==\nT|0|dGFzayA2Mw==\nT|0|dGFzayA2NA==\nT|0|dGFzayA2NQ==\nT|0|dGFzayA2Ng==\nT|0|dGFzayA2Nw==\nT|0|dGFzayA2OA==\nT|0|dGFzayA2OQ==\nT|0|dGFzayA3MA==\nT|0|dGFzayA3MQ==\nT|0|dGFzayA3Mg==\nT|0|dGFzayA3Mw==\nT|0|dGFzayA3NA==\nT|0|dGFzayA3NQ==\nT|0|dGFzayA3Ng==\nT|0|dGFzayA3Nw==\nT|0|dGFzayA3OA==\nT|0|dGFzayA3OQ==\nT|0|dGFzayA4MA==\nT|0|dGFzayA4MQ==\nT|0|dGFzayA4Mg==\nT|0|dGFzayA4Mw==\nT|0|dGFzayA4NA==\nT|0|dGFzayA4NQ==\nT|0|dGFzayA4Ng==\nT|0|dGFzayA4Nw==\nT|0|dGFzayA4OA==\nT|0|dGFzayA4OQ==\nT|0|dGFzayA5MA==\nT|0|dGFzayA5MQ==\nT|0|dGFzayA5Mg==\nT|0|dGFzayA5Mw==\nT|0|dGFzayA5NA==\nT|0|dGFzayA5NQ==\nT|0|dGFzayA5Ng==\nT|0|dGFzayA5Nw==\nT|0|dGFzayA5OA==\nT|0|dGFzayA5OQ==\nT|0|dGFzayAxMDA=\nT|1|dGFzayAxMDE=\n",
      "unchanged": true
    }
  ]
}
```

## Latest test session


- Date/time: 2026-09-15T14:25:21.374152+08:00

- Branch: master

- Commit: bb9a94cf535ccf593878d75438bb2b8c54876377

- Working tree at start:
```text
A  .gitattributes
M  .gitignore
M  README.md
A  build.gradle
A  gradle/wrapper/gradle-wrapper.jar
A  gradle/wrapper/gradle-wrapper.properties
A  gradlew
A  gradlew.bat
A  settings.gradle
M  src/main/java/alpha/Alpha.java
A  src/main/java/alpha/Storage.java
M  src/main/java/alpha/task/Deadline.java
M  src/main/java/alpha/task/Event.java
A  test/run-ui-tests.py
M  test/ui-test-plan.md
A  test/ui-test-session-write-only.md
```

- OS: Linux 6.6.87.2-microsoft-standard-WSL2; UTF-8; 10-second timeout.

- Runtime/compiler:
```text
openjdk version "25.0.3" 2026-04-21
OpenJDK Runtime Environment (build 25.0.3+9-2-24.04.2-Ubuntu)
OpenJDK 64-Bit Server VM (build 25.0.3+9-2-24.04.2-Ubuntu, mixed mode, sharing)
javac 25.0.3
```

- Compile command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/javac -encoding UTF-8 -d /tmp/alpha-ui-8o85fkvr/classes /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/Alpha.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/AlphaException.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/Storage.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Deadline.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Event.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Task.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Todo.java`

- Launch command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/java -Dfile.encoding=UTF-8 -cp /tmp/alpha-ui-8o85fkvr/classes alpha.Alpha`


### UI-01 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-01`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-01: Startup, empty list, and exit ===
INPUT
list
bye
todo ignored
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-02 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-02`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-02: End of input without bye ===
INPUT
list
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
RESULT: PASS
```


### UI-03 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-03`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-03: Add all task types ===
INPUT
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-04 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-04`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-04: Mark, repeat, and unmark ===
INPUT
todo read book
mark 1
mark 1
list
unmark 1
unmark 1
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-05 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-05`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-05: Delete middle, last, first, and only task ===
INPUT
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo borrow book
mark 3
delete 2
list
delete 3
list
delete 1
list
delete 1
list
todo replacement
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
     3.[T][ ] borrow book
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] borrow book
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Noted. I've removed this task:
       [E][X] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 0 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] replacement
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] replacement
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-06 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-06`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-06: Invalid task numbers and recovery ===
INPUT
todo read book
mark
list
mark 
list
mark abc
list
mark 0
list
mark -1
list
mark 2
list
mark 999999999999
list
mark 1 2
list
unmark
list
unmark 
list
unmark abc
list
unmark 0
list
unmark -1
list
unmark 2
list
unmark 999999999999
list
unmark 1 2
list
delete
list
delete 
list
delete abc
list
delete 0
list
delete -1
list
delete 2
list
delete 999999999999
list
delete 1 2
list
mark 1
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Please give me a task number, e.g. "mark 2"...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-07 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-07`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-07: Empty-list task operations ===
INPUT
mark 1
unmark 1
delete 1
list
todo read book
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-08 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-08`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-08: Invalid commands and empty descriptions ===
INPUT
todo read book

dance
todo
todo   
deadline
event
list
todo next
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] next
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] next
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-09 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-09`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-09: Whitespace compatibility ===
INPUT
todo   read book   
mark   1   
list
 list
list 
LIST
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-10 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-10`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-10: Missing time markers compatibility ===
INPUT
deadline return book
event meeting
event lunch /from noon
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: )
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] meeting (from:  to: )
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] lunch (from: noon to: )
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] return book (by: )
     2.[E][ ] meeting (from:  to: )
     3.[E][ ] lunch (from: noon to: )
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-11 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-11`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-11: Grow beyond 100 tasks ===
INPUT
todo task 1
todo task 2
todo task 3
todo task 4
todo task 5
todo task 6
todo task 7
todo task 8
todo task 9
todo task 10
todo task 11
todo task 12
todo task 13
todo task 14
todo task 15
todo task 16
todo task 17
todo task 18
todo task 19
todo task 20
todo task 21
todo task 22
todo task 23
todo task 24
todo task 25
todo task 26
todo task 27
todo task 28
todo task 29
todo task 30
todo task 31
todo task 32
todo task 33
todo task 34
todo task 35
todo task 36
todo task 37
todo task 38
todo task 39
todo task 40
todo task 41
todo task 42
todo task 43
todo task 44
todo task 45
todo task 46
todo task 47
todo task 48
todo task 49
todo task 50
todo task 51
todo task 52
todo task 53
todo task 54
todo task 55
todo task 56
todo task 57
todo task 58
todo task 59
todo task 60
todo task 61
todo task 62
todo task 63
todo task 64
todo task 65
todo task 66
todo task 67
todo task 68
todo task 69
todo task 70
todo task 71
todo task 72
todo task 73
todo task 74
todo task 75
todo task 76
todo task 77
todo task 78
todo task 79
todo task 80
todo task 81
todo task 82
todo task 83
todo task 84
todo task 85
todo task 86
todo task 87
todo task 88
todo task 89
todo task 90
todo task 91
todo task 92
todo task 93
todo task 94
todo task 95
todo task 96
todo task 97
todo task 98
todo task 99
todo task 100
todo task 101
list
delete 100
todo replacement
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 1
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 2
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 3
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 4
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 5
     Now you have 5 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 6
     Now you have 6 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 7
     Now you have 7 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 8
     Now you have 8 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 9
     Now you have 9 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 10
     Now you have 10 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 11
     Now you have 11 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 12
     Now you have 12 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 13
     Now you have 13 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 14
     Now you have 14 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 15
     Now you have 15 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 16
     Now you have 16 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 17
     Now you have 17 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 18
     Now you have 18 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 19
     Now you have 19 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 20
     Now you have 20 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 21
     Now you have 21 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 22
     Now you have 22 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 23
     Now you have 23 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 24
     Now you have 24 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 25
     Now you have 25 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 26
     Now you have 26 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 27
     Now you have 27 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 28
     Now you have 28 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 29
     Now you have 29 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 30
     Now you have 30 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 31
     Now you have 31 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 32
     Now you have 32 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 33
     Now you have 33 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 34
     Now you have 34 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 35
     Now you have 35 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 36
     Now you have 36 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 37
     Now you have 37 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 38
     Now you have 38 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 39
     Now you have 39 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 40
     Now you have 40 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 41
     Now you have 41 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 42
     Now you have 42 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 43
     Now you have 43 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 44
     Now you have 44 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 45
     Now you have 45 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 46
     Now you have 46 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 47
     Now you have 47 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 48
     Now you have 48 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 49
     Now you have 49 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 50
     Now you have 50 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 51
     Now you have 51 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 52
     Now you have 52 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 53
     Now you have 53 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 54
     Now you have 54 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 55
     Now you have 55 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 56
     Now you have 56 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 57
     Now you have 57 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 58
     Now you have 58 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 59
     Now you have 59 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 60
     Now you have 60 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 61
     Now you have 61 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 62
     Now you have 62 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 63
     Now you have 63 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 64
     Now you have 64 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 65
     Now you have 65 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 66
     Now you have 66 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 67
     Now you have 67 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 68
     Now you have 68 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 69
     Now you have 69 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 70
     Now you have 70 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 71
     Now you have 71 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 72
     Now you have 72 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 73
     Now you have 73 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 74
     Now you have 74 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 75
     Now you have 75 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 76
     Now you have 76 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 77
     Now you have 77 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 78
     Now you have 78 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 79
     Now you have 79 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 80
     Now you have 80 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 81
     Now you have 81 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 82
     Now you have 82 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 83
     Now you have 83 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 84
     Now you have 84 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 85
     Now you have 85 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 86
     Now you have 86 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 87
     Now you have 87 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 88
     Now you have 88 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 89
     Now you have 89 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 90
     Now you have 90 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 91
     Now you have 91 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 92
     Now you have 92 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 93
     Now you have 93 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 94
     Now you have 94 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 95
     Now you have 95 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 96
     Now you have 96 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 97
     Now you have 97 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 98
     Now you have 98 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 99
     Now you have 99 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 100
     Now you have 100 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] task 101
     Now you have 101 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 100
     101.[T][ ] task 101
    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] task 100
     Now you have 100 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] replacement
     Now you have 101 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 101
     101.[T][ ] replacement
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-12 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-12`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-12: Save after every task change ===
INPUT
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
mark 1
unmark 1
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-13 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-13`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-13: Read-only commands and rejected changes do not create data ===
INPUT
list
todo
mark 1
unknown
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bro, please add a description...
    ____________________________________________________________
     There is no task with that number...
    ____________________________________________________________
     Bro, I don't know what that means...
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-14 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-14`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-14: Restore all task types and update the saved list ===
INPUT
list
unmark 3
mark 2
todo café
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: June 6th)
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] café
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     4.[T][ ] café
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-15 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-15`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-15: Load an empty save file ===
INPUT
list
todo read book
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-16 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-16`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-16: Restore the versioned snapshot and update it ===
INPUT
list
unmark 3
mark 2
todo café
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: June 6th)
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] café
     Now you have 4 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     4.[T][ ] café
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-17 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-17`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-17: First run with an existing data folder ===
INPUT
list
todo read book
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-18 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-18`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-18: Reject corrupted completion status ===
INPUT
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Invalid field count or completion status. The file has not been changed.
    ____________________________________________________________
RESULT: PASS
```


### UI-19 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-19`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-19: Reject an incomplete event record ===
INPUT
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown task type or incorrect field count. The file has not been changed.
    ____________________________________________________________
RESULT: PASS
```


### UI-20 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-20`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-20: Reject an unknown task type ===
INPUT
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown task type or incorrect field count. The file has not been changed.
    ____________________________________________________________
RESULT: PASS
```


### UI-21 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-21`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-21: Preserve a partially valid legacy snapshot ===
INPUT
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Cannot load data/alpha.txt: Invalid task at line 2: Unknown file version or invalid legacy record. The file has not been changed.
    ____________________________________________________________
RESULT: PASS
```


### UI-22 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-22`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-22: Persist deletion of restored tasks ===
INPUT
delete 2
list
delete 1
delete 1
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[E][ ] meeting (from: Monday to: Tuesday)
    ____________________________________________________________
     Noted. I've removed this task:
       [T][X] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
     Noted. I've removed this task:
       [E][ ] meeting (from: Monday to: Tuesday)
     Now you have 0 tasks in the list.
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


### UI-23 result


- Working directory: `/tmp/alpha-ui-8o85fkvr/UI-23`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-23: Restore more than 100 tasks ===
INPUT
mark 101
list
bye
OUTPUT
 █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ 
██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗
███████║██║     ██████╔╝███████║███████║
██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║
██║  ██║███████╗██║     ██║  ██║██║  ██║
╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝
Yooo! I'm Alpha. What can I help you with today?
    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] task 101
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] task 1
     2.[T][ ] task 2
     3.[T][ ] task 3
     4.[T][ ] task 4
     5.[T][ ] task 5
     6.[T][ ] task 6
     7.[T][ ] task 7
     8.[T][ ] task 8
     9.[T][ ] task 9
     10.[T][ ] task 10
     11.[T][ ] task 11
     12.[T][ ] task 12
     13.[T][ ] task 13
     14.[T][ ] task 14
     15.[T][ ] task 15
     16.[T][ ] task 16
     17.[T][ ] task 17
     18.[T][ ] task 18
     19.[T][ ] task 19
     20.[T][ ] task 20
     21.[T][ ] task 21
     22.[T][ ] task 22
     23.[T][ ] task 23
     24.[T][ ] task 24
     25.[T][ ] task 25
     26.[T][ ] task 26
     27.[T][ ] task 27
     28.[T][ ] task 28
     29.[T][ ] task 29
     30.[T][ ] task 30
     31.[T][ ] task 31
     32.[T][ ] task 32
     33.[T][ ] task 33
     34.[T][ ] task 34
     35.[T][ ] task 35
     36.[T][ ] task 36
     37.[T][ ] task 37
     38.[T][ ] task 38
     39.[T][ ] task 39
     40.[T][ ] task 40
     41.[T][ ] task 41
     42.[T][ ] task 42
     43.[T][ ] task 43
     44.[T][ ] task 44
     45.[T][ ] task 45
     46.[T][ ] task 46
     47.[T][ ] task 47
     48.[T][ ] task 48
     49.[T][ ] task 49
     50.[T][ ] task 50
     51.[T][ ] task 51
     52.[T][ ] task 52
     53.[T][ ] task 53
     54.[T][ ] task 54
     55.[T][ ] task 55
     56.[T][ ] task 56
     57.[T][ ] task 57
     58.[T][ ] task 58
     59.[T][ ] task 59
     60.[T][ ] task 60
     61.[T][ ] task 61
     62.[T][ ] task 62
     63.[T][ ] task 63
     64.[T][ ] task 64
     65.[T][ ] task 65
     66.[T][ ] task 66
     67.[T][ ] task 67
     68.[T][ ] task 68
     69.[T][ ] task 69
     70.[T][ ] task 70
     71.[T][ ] task 71
     72.[T][ ] task 72
     73.[T][ ] task 73
     74.[T][ ] task 74
     75.[T][ ] task 75
     76.[T][ ] task 76
     77.[T][ ] task 77
     78.[T][ ] task 78
     79.[T][ ] task 79
     80.[T][ ] task 80
     81.[T][ ] task 81
     82.[T][ ] task 82
     83.[T][ ] task 83
     84.[T][ ] task 84
     85.[T][ ] task 85
     86.[T][ ] task 86
     87.[T][ ] task 87
     88.[T][ ] task 88
     89.[T][ ] task 89
     90.[T][ ] task 90
     91.[T][ ] task 91
     92.[T][ ] task 92
     93.[T][ ] task 93
     94.[T][ ] task 94
     95.[T][ ] task 95
     96.[T][ ] task 96
     97.[T][ ] task 97
     98.[T][ ] task 98
     99.[T][ ] task 99
     100.[T][ ] task 100
     101.[T][X] task 101
    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
RESULT: PASS
```


Overall: PASS

- UI-01: PASS
- UI-02: PASS
- UI-03: PASS
- UI-04: PASS
- UI-05: PASS
- UI-06: PASS
- UI-07: PASS
- UI-08: PASS
- UI-09: PASS
- UI-10: PASS
- UI-11: PASS
- UI-12: PASS
- UI-13: PASS
- UI-14: PASS
- UI-15: PASS
- UI-16: PASS
- UI-17: PASS
- UI-18: PASS
- UI-19: PASS
- UI-20: PASS
- UI-21: PASS
- UI-22: PASS
- UI-23: PASS

