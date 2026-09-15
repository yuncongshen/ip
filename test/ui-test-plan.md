# UI Test Plan

This file is the source of record for Alpha command-line UI test cases and their latest execution results.
The cases below are authored expectations, not evidence of an executed test session.

## Test environment

- Java version: 25. Record the exact runtime version for each session.
- Source directory: `src/main/java`; entry point: `alpha.Alpha`.
- Compile all production Java source files with UTF-8 encoding into a fresh temporary output directory.
- Start a fresh application process for every case, with that temporary directory as its working directory and classpath.
- Use UTF-8 for standard input and captured output. On Java 25, use `-Dfile.encoding=UTF-8`.
- Send the input block literally, including blank lines and trailing spaces. End the final line with a newline, then close stdin.
- A blank line in an input block is an empty command, not immediate EOF.
- Timeout: 10 seconds per case. Capture stdout, stderr, exit code, and timeout status separately.
- Compare stdout exactly after normalizing CRLF to LF. Do not trim spaces, blank lines, or the final newline.
- Expected output includes the banner, greeting, dividers, and final newline. It excludes terminal prompts and echoed input.
- The first banner line intentionally ends with a space; preserve it when editing or extracting fixtures.
- Unexpected stderr, a nonzero exit code, a timeout, or any stdout mismatch is a failure.

## Execution and maintenance rules

1. Verify Java 25, compile, and record the commit hash plus whether the working tree has changes.
2. Run cases in the order below. Each has an aim, exact inputs, and complete expected stdout; never use ellipses as fixture data.
3. Stop immediately if compilation, launch, or a case fails. Mark every remaining case NOT RUN.
4. On a mismatch, retain complete expected and actual output and identify the first differing line, including whitespace differences.
5. Retain the input/output console record for each executed case and update Latest test session.
6. Declare PASS only when every listed case has executed successfully. An empty or partially run suite cannot pass.
7. Preserve stable case IDs. When requirements change, review and update expectations deliberately; do not replace them with captured output merely to make a test pass.

## Expected behavior and scope

- Core cases cover task creation, listing, status changes, one-based deletion, invalid-input recovery, and dynamic collection growth.
- UI-09 and UI-10 characterize existing parser behavior. Whitespace tolerance and required time markers have not been specified as new acceptance requirements.
- In particular, missing time markers currently produce empty time fields, while bare mark/unmark commands use the unknown-command error. These baselines do not endorse those UX choices.
- If stricter time validation or more flexible command whitespace is adopted, update these compatibility cases together with the requirement and implementation.
- Blank commands are expected to produce the existing unknown-command error. EOF exits without a farewell; bye prints a farewell and ignores subsequent input.
- Save/load tests belong to Level-7 and should be added when persistence is implemented, with an isolated data directory for each case.

## Coverage index

| Case | Coverage |
|---|---|
| UI-01 | Startup, empty list, and exit |
| UI-02 | End of input without bye |
| UI-03 | Add all task types |
| UI-04 | Mark, repeat, and unmark |
| UI-05 | Delete middle, last, first, and only task |
| UI-06 | Invalid task numbers and recovery |
| UI-07 | Empty-list task operations |
| UI-08 | Invalid commands and empty descriptions |
| UI-09 | Whitespace compatibility |
| UI-10 | Missing time markers compatibility |
| UI-11 | Grow beyond 100 tasks |

## Required test case format

Each case has a stable UI-NN heading, an Aim paragraph, one Inputs text block, and one Expected output text block.
Both text blocks contain literal fixture data, including their final newline. Keep any explanations outside the blocks.
For long cases such as UI-11, the full inputs and outputs remain expanded so they can be executed without interpretation.

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
     Bro, I don't know what that means...
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
     Bro, I don't know what that means...
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

## Latest test session

Status: NOT RUN. These cases have been authored but have not been executed as a UI test session.
Earlier ad hoc command-dispatcher checks do not count as execution of this plan.

| Case | Result |
|---|---|
| UI-01 | NOT RUN |
| UI-02 | NOT RUN |
| UI-03 | NOT RUN |
| UI-04 | NOT RUN |
| UI-05 | NOT RUN |
| UI-06 | NOT RUN |
| UI-07 | NOT RUN |
| UI-08 | NOT RUN |
| UI-09 | NOT RUN |
| UI-10 | NOT RUN |
| UI-11 | NOT RUN |

For the next session, replace this section with:

- Date/time with timezone.
- Branch, full commit hash, and working-tree state (including relevant uncommitted changes).
- OS, exact Java runtime/compiler versions, encoding, compile command, launch command, and temporary working directory.
- Overall result, executed case IDs, first failure/stop point, and remaining NOT RUN cases.
- Per-case exit code, stderr, timeout status, and first differing line if applicable.
- A console record for every executed case using the format below. Retain complete expected output separately on failure.

```text
=== UI-NN: Case title ===
INPUT
<complete literal input>
OUTPUT
<complete actual stdout>
RESULT: PASS or FAIL
```
