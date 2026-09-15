# Historical write-only UI test session

These results cover the earlier writing implementation only. They do not validate startup loading.

## Recorded session



- Date/time: 2026-09-15T12:12:17.277980+08:00

- Branch: branch-Level-7

- Commit: 4f675482d08d7602ee73a0173009124525e6aa92

- Working tree at start:
```text
M .gitignore
 M AGENTS.md
 M CONTRIBUTORS.md
 M README.md
 M docs/README.md
 M src/main/java/alpha/Alpha.java
 M src/main/java/alpha/AlphaException.java
 M src/main/java/alpha/task/Deadline.java
 M src/main/java/alpha/task/Event.java
 M src/main/java/alpha/task/Task.java
 M src/main/java/alpha/task/Todo.java
 M test/ui-test-plan.md
?? test/run-ui-tests.py
```

- OS: Linux 6.6.87.2-microsoft-standard-WSL2; UTF-8; 10-second timeout.

- Runtime/compiler:
```text
openjdk version "25.0.3" 2026-04-21
OpenJDK Runtime Environment (build 25.0.3+9-2-24.04.2-Ubuntu)
OpenJDK 64-Bit Server VM (build 25.0.3+9-2-24.04.2-Ubuntu, mixed mode, sharing)
javac 25.0.3
```

- Compile command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/javac -encoding UTF-8 -d /tmp/alpha-ui-7gi0yiur/classes /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/Alpha.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/AlphaException.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Deadline.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Event.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Task.java /mnt/c/Users/sheny/CS2113/ip/src/main/java/alpha/task/Todo.java`

- Launch command: `/usr/lib/jvm/java-25-openjdk-amd64/bin/java -Dfile.encoding=UTF-8 -cp /tmp/alpha-ui-7gi0yiur/classes alpha.Alpha`


### UI-12 result


- Working directory: `/tmp/alpha-ui-7gi0yiur/UI-12`

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


- Working directory: `/tmp/alpha-ui-7gi0yiur/UI-13`

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


- Working directory: `/tmp/alpha-ui-7gi0yiur/UI-14`

- Exit: 0; timeout: False; stderr: ''.

```text
=== UI-14: Replace an old snapshot without loading it ===
INPUT
list
todo café
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
       [T][ ] café
     Now you have 1 tasks in the list.
    ____________________________________________________________
RESULT: PASS
```


Overall: PASS

- UI-12: PASS
- UI-13: PASS
- UI-14: PASS

