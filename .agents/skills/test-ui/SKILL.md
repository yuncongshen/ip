---
name: test-ui
description: Run this project's command-line UI tests from lists of inputs and expected outputs, maintain test/ui-test-plan.md, stop at the first failure, and report the console session.
---

# Test UI

Run repeatable command-line UI tests for the Alpha application. Treat `test/ui-test-plan.md` as the source of record
for test cases, execution settings, and the latest test session.

## Record the tests

- Accept one or more test cases supplied by the user. A test case may contain one command or an ordered sequence of
  commands for a single program session.
- Before running them, record every supplied test case in `test/ui-test-plan.md`. Assign stable identifiers such as
  `UI-01` and preserve existing cases unless the user explicitly asks to replace or remove them.
- Record an aim, the exact input lines, and the complete expected standard output for every test case. Do not run a
  test with any of those fields missing.
- Record relevant execution information in the plan, including the Java version, entry point, isolation policy,
  timeout, and output-comparison rules. Update it when the project setup changes.

## Prepare the session

- Read `test/ui-test-plan.md`, the applicable project instructions, and the program's documented run setup.
- Use Java 25. Compile all source files into a fresh temporary output directory so testing does not add build artifacts
  to the repository. Launch the `alpha.Alpha` entry point from that directory.
- If compilation or launch fails, stop without running further cases. Report the command output as the actual result
  and explain that a test session could not be completed.

## Run and compare

- Run test cases in plan order. Start a fresh program process for each test case so state cannot leak between cases.
- Send the recorded input lines to standard input in their exact order and close standard input after the final line.
- Capture standard output, standard error, the exit code, and whether the process exceeded the recorded timeout.
- Normalize only operating-system line endings to `\n`. Otherwise compare standard output exactly, including spaces
  and blank lines. Unexpected standard error, a non-zero exit code, or a timeout is a failure.
- Stop the entire session immediately when a test fails. Do not run later cases, and mark them as not run.

## Record and report the session

- After each executed case, retain a console record in this form:

  ```text
  === UI-01: Test case name ===
  INPUT
  first command
  second command
  OUTPUT
  complete actual output
  RESULT: PASS
  ```

- Replace `RESULT: PASS` with `RESULT: FAIL` when applicable. This display separates piped input from captured output
  while preserving both exactly.
- Update the `Latest test session` section of `test/ui-test-plan.md` with the date and time, environment, executed case
  identifiers, overall result, stop point if any, and the console record for every executed case.
- Show the same console input/output record in the response after testing.
- On failure, report the complete actual output and complete expected output in separate code blocks, identify the
  first differing line when possible, list remaining cases as not run, and end the test session immediately.

## Completion criteria

A session passes only when every recorded test case runs and matches its expected output. Never claim that tests pass
when Java 25 is unavailable, compilation fails, a process cannot launch, or any case remains unexecuted.
