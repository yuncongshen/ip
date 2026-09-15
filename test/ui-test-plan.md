# UI Test Plan

This branch tests the Level-7 saving and startup-loading implementation. It starts independently from master.
UI-01 through UI-11 remain on branch-Level-6; they are not part of this branch's suite yet.
After the branches are merged, combine both sets and add delete-persistence coverage.

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
Delete persistence remains on the parallel Level-6 branch. Save failures now roll back the command; atomic replacement and external-edit checks protect existing snapshots.
UI-14 covers restoring and updating existing tasks; UI-15 covers an empty file. UI-12 and UI-13 also cover startup without a save file.
UI-14 covers legacy import and conversion; UI-16 covers the ALPHA-1 format. UI-17 adds the missing-file/existing-folder case. UI-18 through UI-21 cover corrupted snapshots and verify the original data survives.

Pending error-path coverage: malformed UTF-8/Base64, empty descriptions, unknown versions, ambiguous legacy fields, BOM/blank lines/CRLF, 100 versus 101 tasks, the 1 MiB boundary, permission denial, full disks, directories or symlinks at the save path, external edits, overlapping saves, unsupported atomic moves, rollback, and temporary-file cleanup.
These scenarios are a coverage backlog, not executed tests or claims of verified behavior.

## Test cases

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

## Latest test session


- Date/time: 2026-09-15T12:33:54.890609+08:00

- Branch: branch-Level-7

- Commit: 4f675482d08d7602ee73a0173009124525e6aa92

- Working tree at start:
```text
 M .gitignore
 M README.md
 M src/main/java/alpha/Alpha.java
 M src/main/java/alpha/task/Deadline.java
 M src/main/java/alpha/task/Event.java
 M test/ui-test-plan.md
?? src/main/java/alpha/Storage.java
?? test/run-ui-tests.py
?? test/ui-test-session-write-only.md
```

- OS: Linux 6.6.87.2-microsoft-standard-WSL2; UTF-8; 10-second timeout.

- Runtime/compiler:
```text
openjdk version "25.0.3" 2026-04-21
OpenJDK Runtime Environment (build 25.0.3+9-2-24.04.2-Ubuntu)
OpenJDK 64-Bit Server VM (build 25.0.3+9-2-24.04.2-Ubuntu, mixed mode, sharing)
javac 25.0.3
```

- Compile command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/javac -encoding UTF-8 -d /tmp/alpha-ui-qz10lrrq/classes /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/Alpha.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/AlphaException.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/Storage.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Deadline.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Event.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Task.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Todo.java`

- Launch command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/java -Dfile.encoding=UTF-8 -cp /tmp/alpha-ui-qz10lrrq/classes alpha.Alpha`


### UI-12 result


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-12`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-13`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-14`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-15`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-16`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-17`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-18`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-19`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-20`

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


- Working directory: `/tmp/alpha-ui-qz10lrrq/UI-21`

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


Overall: PASS

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

