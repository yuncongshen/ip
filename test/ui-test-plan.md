# UI Test Plan

This file is the source of record for Alpha command-line UI test cases and their latest execution results.

## Test environment

- Java version: 25
- Source directory: `src/main/java`
- Entry point: `alpha.Alpha`
- Compilation: Compile all Java source files into a fresh temporary output directory.
- Isolation: Start a fresh application process for every test case.
- Input: Send the listed lines in order, then close standard input.
- Timeout: 10 seconds per test case.
- Output comparison: Compare standard output exactly after normalizing line endings to `\n`.
- Additional failure conditions: Standard error output, a non-zero exit code, or a timeout.

## Required test case format

Each recorded test case uses the following structure:

````markdown
### UI-NN: Descriptive name

**Aim:** State the behavior being verified.

**Inputs:**

```text
one command per line
```

**Expected output:**

```text
complete expected standard output
```
````

## Test cases

No UI test cases have been supplied yet.

## Latest test session

No UI test session has been run yet.
