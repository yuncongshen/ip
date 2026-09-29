---
layout: default
title: Alpha User Guide
permalink: /
---

# Alpha User Guide

**Alpha** is a command-line chatbot that keeps track of your tasks, deadlines, and events.
Type a command, press Enter, and Alpha saves your changes automatically.

[Get started](#get-started) · [Commands](#commands) · [Dates and times](#dates-and-times) · [Saving and troubleshooting](#saving-and-troubleshooting)

## Get started

You need **JDK 25**. Check your installation with `java -version` in a terminal.

To build the version described in this guide:

1. [Download the project source](https://github.com/yuncongshen/ip/archive/refs/heads/master.zip) and extract it.
2. Open a terminal in the extracted project folder (the folder containing `gradlew`).
3. Build Alpha. The first build needs an internet connection:

   | Operating system | Command |
   | --- | --- |
   | Windows PowerShell | `.\gradlew.bat shadowJar` |
   | macOS or Linux | `sh ./gradlew shadowJar` |

4. Start Alpha from the same folder:

   ```text
   java -jar build/libs/alpha.jar
   ```

When the greeting appears, try:

```text
todo read book
deadline return book /by 2/12/2019 1800
list
mark 1
find book
bye
```

If you already have a current `alpha.jar`, open a terminal in its folder and run `java -jar alpha.jar`.
Older release JARs may not include all the features below.

## Commands

Use lowercase command names and enter one command per line. Replace example descriptions, dates,
and task numbers with your own. Include the spaces around `/by`, `/from`, and `/to` as shown.

### Add a task: `todo`

```text
todo read book
```

Adds a task without a date. A description is required.

### Add a deadline: `deadline`

```text
deadline return book /by 2019-12-02
deadline submit report /by 2/12/2019 1800
```

Adds a task with a required date and an optional time. The second example displays as:

```text
[D][ ] submit report (by: Dec 02 2019, 18:00)
```

See [Dates and times](#dates-and-times) for accepted formats.

### Add an event: `event`

```text
event project meeting /from Monday 2pm /to Monday 4pm
```

Adds an event with a description and start/end text. Event times are free text, so Alpha does not
validate or compare them as dates. You can omit them, but supplying both makes your list clearer.

### View your tasks: `list`

```text
list
```

Shows all tasks in insertion order. For example:

```text
1.[T][X] read book
2.[D][ ] return book (by: Dec 02 2019)
3.[E][ ] project meeting (from: Monday 2pm to: Monday 4pm)
```

`[T]` means todo, `[D]` deadline, and `[E]` event. `[X]` means done; `[ ]` means not done.

### Mark or unmark a task

```text
mark 2
unmark 2
```

`mark` sets task 2 to done; `unmark` sets it back to not done. Repeating either command is allowed.

### Delete a task: `delete`

```text
delete 2
```

Removes task 2 and saves the updated list. There is no undo command. Later tasks move up one number,
so use `list` again before deleting another task if you are unsure of its number.

### Search descriptions: `find`

```text
find book
find return book
```

Searches descriptions across all task types, including completed tasks. Matching ignores case:
`book` matches `BOOK` and `notebook`. Multiple words form one phrase; punctuation is literal.
Dates and time fields are not searched. An empty search is rejected; no matches produces a clear message.

### Find deadlines on a date: `on`

```text
on 2019-12-02
on 2/12/2019
```

Both list deadlines on 2 December 2019, including completed tasks and every time that day.
Supply a date without a time. Todos and events are excluded.

**Search results keep the task numbers from the full list.** Use those numbers with `mark`, `unmark`,
and `delete`; gaps in result numbering are normal. Searching does not change your tasks.

### Exit: `bye`

```text
bye
```

Closes Alpha. Successful task changes are already saved; there is no separate save command.

## Dates and times

| Deadline input | Meaning |
| --- | --- |
| `2019-12-02` | 2 December 2019, no time specified |
| `2/12/2019` | The same date; slash dates are always day/month/year |
| `02/12/2019 1800` | 2 December 2019 at 6pm |
| `2/12/2019 18:00` | The same date and time |
| `2019-12-02T18:00` | The same date and time in ISO format |

Use four digits for the year. ISO dates need two-digit months and days; slash dates accept one or two.
Times use the 24-hour clock: `0000` or `00:00` is midnight; `2359` or `23:59` is 11:59pm.
Alpha rejects impossible dates such as `2023-02-29` and invalid times such as `2400`.
Words such as `tomorrow`, AM/PM times, seconds, and time zones are not accepted for deadlines.

Dates display with English month names. A date without a time stays date-only; an explicit midnight
displays `00:00`. The `on` command accepts either date-only format in the table.

## Saving and troubleshooting

Alpha saves to `data/alpha.txt` inside the folder from which you launched it, and loads that file at startup.
The folder and file are created on your first successful task change. Keep launching from the same folder
to see the same tasks. Back up `data/alpha.txt` to keep a copy of your list.

| Problem | What to do |
| --- | --- |
| Java is missing or the JAR reports an unsupported class version | Install/select JDK 25 and check `java -version`. |
| The JAR cannot be found | Build it first and run the start command from the project folder. |
| Alpha does not understand a command | Check the lowercase command name and spaces. Enter the corrected command. |
| A task number is rejected | Run `list` and use a positive number shown there. |
| A deadline date is rejected | Use one of the formats above and check the calendar date and time. |
| Saved tasks seem missing | Check the folder from which you launched Alpha; each folder has its own data file. |
| Alpha cannot load a data file | It leaves the file untouched. Back it up, then correct the reported line or restore a valid backup. Older free-text deadlines such as `June 6th` need a full date including the year. |
| Alpha cannot save a change | The change is not applied. Check folder permissions and available disk space, then retry. If the file changed outside Alpha, restart to load it first. |

Avoid editing the data file while Alpha is running. An invalid command leaves the task list unchanged.
