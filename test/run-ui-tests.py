"""Run the UI plan with Java 25 and stop on the first failed case."""

import argparse
from datetime import datetime
import json
import os
import platform
from pathlib import Path
import queue
import re
import subprocess
import tempfile
import threading
import time


ROOT = Path(__file__).resolve().parents[1]
PLAN = ROOT / "test/ui-test-plan.md"
DIVIDER = "    ____________________________________________________________\n"


def normalize(text):
    return text.replace("\r\n", "\n")


def block(body, label, language):
    match = re.search(r"\*\*" + re.escape(label) + r":\*\*\s+```" + language + r"\n(.*?)```", body, re.S)
    if match is None:
        raise ValueError("Missing fixture: " + label)
    return match.group(1)


def file_state(directory):
    path = directory / "data/alpha.txt"
    if not path.exists():
        return None, None
    return normalize(path.read_text(encoding="utf-8")), path.stat().st_mtime_ns


def verify_file(directory, checkpoint, previous):
    actual = file_state(directory)
    if actual[0] != checkpoint["contents"]:
        raise AssertionError(f"File mismatch: expected {checkpoint['contents']!r}, actual {actual[0]!r}")
    if checkpoint.get("unchanged") and actual != previous:
        raise AssertionError("A read-only or rejected command rewrote the data file")
    expected_directory = checkpoint.get("directoryExists", checkpoint["contents"] is not None)
    if (directory / "data").is_dir() != expected_directory:
        raise AssertionError("Unexpected data-directory state")
    return actual


def run_case(case, java, classes, directory):
    if case["files"] is None:
        return run_console_case(case, java, classes, directory)
    initial = case["files"]["initialFile"]
    if case["files"].get("initialDirectory"):
        (directory / "data").mkdir()
    if initial is not None:
        (directory / "data").mkdir(exist_ok=True)
        path = directory / "data/alpha.txt"
        path.write_text(initial, encoding="utf-8")
        # Make an accidental rewrite distinguishable even on coarse timestamp filesystems.
        os.utime(path, (946684800, 946684800))
    previous = file_state(directory)
    output_lines = []
    lines = queue.Queue()
    process = subprocess.Popen(
        [str(java), "-Dfile.encoding=UTF-8", "-cp", str(classes), "alpha.Alpha"],
        cwd=directory, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        text=True, encoding="utf-8",
    )

    def read_output():
        try:
            for line in process.stdout:
                output_lines.append(line)
                lines.put(line)
        finally:
            lines.put(None)

    reader = threading.Thread(target=read_output, daemon=True)
    reader.start()
    deadline = time.monotonic() + 10
    failure = None
    timed_out = False

    def wait_for_divider():
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise TimeoutError("Case exceeded 10 seconds")
            try:
                line = lines.get(timeout=remaining)
            except queue.Empty as exception:
                raise TimeoutError("No complete response within 10 seconds") from exception
            if line is None:
                raise AssertionError("Application exited before completing its response")
            if line == DIVIDER:
                return

    try:
        wait_for_divider()
        initial_checkpoint = {"contents": initial, "unchanged": True,
                              "directoryExists": initial is not None or case["files"].get("initialDirectory", False)}
        previous = verify_file(directory, initial_checkpoint, previous)
        for number, command in enumerate(case["inputs"].splitlines(), 1):
            process.stdin.write(command + "\n")
            process.stdin.flush()
            wait_for_divider()
            checkpoint = case["files"]["steps"][number - 1]
            previous = verify_file(directory, checkpoint, previous)
        process.stdin.close()
        process.wait(timeout=max(0.001, deadline - time.monotonic()))
        final_checkpoint = case["files"]["steps"][-1] if case["files"]["steps"] else initial_checkpoint
        verify_file(directory, {**final_checkpoint, "unchanged": True}, previous)
    except (TimeoutError, subprocess.TimeoutExpired) as exception:
        timed_out = True
        failure = str(exception)
    except (AssertionError, OSError) as exception:
        failure = str(exception)
    finally:
        if process.poll() is None:
            process.kill()
        process.wait()
        reader.join(timeout=1)
        stderr = process.stderr.read()
        for stream in (process.stdin, process.stdout, process.stderr):
            stream.close()

    actual = normalize("".join(output_lines))
    if failure is None and (stderr or process.returncode != 0):
        failure = f"Exit code {process.returncode}; stderr: {stderr!r}"
    if actual != case["expected"]:
        expected_lines = case["expected"].splitlines(keepends=True)
        actual_lines = actual.splitlines(keepends=True)
        first = next((i + 1 for i in range(max(len(expected_lines), len(actual_lines)))
                      if expected_lines[i:i + 1] != actual_lines[i:i + 1]), None)
        failure = (failure + "; " if failure else "") + f"Stdout differs at line {first}"
    return actual, stderr, process.returncode, timed_out, failure


def run_console_case(case, java, classes, directory):
    """Run inherited console-only cases, including input that follows bye."""
    try:
        result = subprocess.run(
            [str(java), "-Dfile.encoding=UTF-8", "-cp", str(classes), "alpha.Alpha"],
            input=case["inputs"], cwd=directory, capture_output=True, text=True, encoding="utf-8", timeout=10)
    except subprocess.TimeoutExpired as exception:
        actual = exception.stdout or b""
        if isinstance(actual, bytes):
            actual = actual.decode("utf-8", errors="replace")
        return normalize(actual), "", None, True, "Case exceeded 10 seconds"
    actual = normalize(result.stdout)
    failure = None
    if result.returncode or result.stderr:
        failure = f"Exit code {result.returncode}; stderr: {result.stderr!r}"
    if actual != case["expected"]:
        expected_lines = case["expected"].splitlines(keepends=True)
        actual_lines = actual.splitlines(keepends=True)
        first = next(i + 1 for i in range(max(len(expected_lines), len(actual_lines)))
                     if expected_lines[i:i + 1] != actual_lines[i:i + 1])
        failure = (failure + "; " if failure else "") + f"Stdout differs at line {first}"
    return actual, result.stderr, result.returncode, False, failure


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", required=True, type=Path)
    args = parser.parse_args()
    suffix = ".exe" if os.name == "nt" else ""
    java = args.java_home / "bin" / ("java" + suffix)
    javac = args.java_home / "bin" / ("javac" + suffix)
    document = normalize(PLAN.read_text(encoding="utf-8"))
    prefix = document.split("## Latest test session", 1)[0]
    cases = []
    for match in re.finditer(r"^### (UI-\d+): ([^\n]+)\n(.*?)(?=^### |^## |\Z)", prefix, re.M | re.S):
        body = match[3]
        if "**Aim:**" not in body:
            raise ValueError("Missing aim: " + match[1])
        case = {"id": match[1], "title": match[2], "inputs": block(body, "Inputs", "text"),
                "expected": block(body, "Expected output", "text"),
                "files": json.loads(block(body, "File checkpoints", "json")) if "**File checkpoints:**" in body else None}
        if case["files"] is not None and [step["afterInput"] for step in case["files"]["steps"]] != list(range(1, len(case["inputs"].splitlines()) + 1)):
            raise ValueError("Every command must have a file checkpoint: " + case["id"])
        cases.append(case)
    if not cases:
        raise ValueError("No complete cases found")

    git = lambda *arguments: subprocess.check_output(
        ["git", "-c", "core.filemode=false", "-c", "core.autocrlf=true", *arguments], cwd=ROOT, text=True).rstrip()
    session = ["## Latest test session\n", f"- Date/time: {datetime.now().astimezone().isoformat()}",
               f"- Branch: {git('branch', '--show-current')}", f"- Commit: {git('rev-parse', 'HEAD')}",
               "- Working tree at start:\n```text\n" + git("status", "--short") + "\n```",
               f"- OS: {platform.system()} {platform.release()}; UTF-8; 10-second timeout."]
    results = {case["id"]: "NOT RUN" for case in cases}
    try:
        version = subprocess.run([str(java), "-version"], capture_output=True, text=True, check=True)
        compiler = subprocess.run([str(javac), "-version"], capture_output=True, text=True, check=True)
        runtime_version = version.stdout + version.stderr
        compiler_version = compiler.stdout + compiler.stderr
        session.append("- Runtime/compiler:\n```text\n" + runtime_version + compiler_version + "```")
        if not re.search(r'version "25(?:\.|\")', runtime_version) or not compiler_version.startswith("javac 25"):
            raise RuntimeError("Java 25 is required")
        with tempfile.TemporaryDirectory(prefix="alpha-ui-") as temporary:
            root = Path(temporary)
            classes = root / "classes"
            classes.mkdir()
            sources = sorted((ROOT / "src/main/java").rglob("*.java"))
            command = [str(javac), "-encoding", "UTF-8", "-d", str(classes), *map(str, sources)]
            session.append("- Compile command: `" + " ".join(command) + "`")
            compilation = subprocess.run(command, capture_output=True, text=True)
            if compilation.returncode:
                raise RuntimeError("Compilation failed:\n" + compilation.stdout + compilation.stderr)
            session.append(f"- Launch command: `{java} -Dfile.encoding=UTF-8 -cp {classes} alpha.Alpha`")
            for case in cases:
                directory = root / case["id"]
                directory.mkdir()
                try:
                    actual, stderr, code, timed_out, failure = run_case(case, java, classes, directory)
                except OSError as exception:
                    raise RuntimeError(f"Launch failed for {case['id']}: {exception}") from exception
                result = "FAIL" if failure else "PASS"
                results[case["id"]] = result
                record = (f"=== {case['id']}: {case['title']} ===\nINPUT\n{case['inputs']}"
                          f"OUTPUT\n{actual}RESULT: {result}\n")
                session.extend([f"\n### {case['id']} result\n", f"- Working directory: `{directory}`",
                                f"- Exit: {code}; timeout: {timed_out}; stderr: {stderr!r}.",
                                "```text\n" + record + "```"])
                if failure:
                    session.extend(["Failure: " + failure, "Expected stdout:\n```text\n" + case["expected"] + "```"])
                    break
    except (OSError, subprocess.SubprocessError, RuntimeError) as exception:
        session.append("Session stopped: " + str(exception))
    passed = all(result == "PASS" for result in results.values())
    summary = "\nOverall: " + ("PASS" if passed else "INCOMPLETE OR FAILED") + "\n\n"
    summary += "\n".join(f"- {key}: {value}" for key, value in results.items()) + "\n"
    session.append(summary)
    report = "\n\n".join(session) + "\n"
    PLAN.write_text(prefix + report, encoding="utf-8")
    print(report)
    return 0 if passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
