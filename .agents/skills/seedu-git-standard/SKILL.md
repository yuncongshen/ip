---
name: seedu-git-standard
description: Apply the SE-EDU Git conventions when proposing or creating commit messages and branch names for this project.
---

# SE-EDU Git Standard

Follow the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html) for this project.
Following this skill does not grant permission to commit, push, create a branch, or otherwise change repository state.

## Commit subject

- Give every commit a clear, well-written subject.
- Aim for 50 characters or fewer and never exceed 72 characters.
- Use the imperative mood, as if completing the sentence "If applied, this commit will ...". For example, write
  `Add README.md`, not `Added README.md` or `Adding README.md`.
- Capitalize the first word of the subject and do not end it with a period.
- Add an optional `<scope>:` or `<category>:` prefix only when it improves clarity. The prefix itself may be lowercase;
  capitalize the imperative subject after it, for example, `bug fix: Add space after name`.
- Conventional Commits is an optional alternative, not a project requirement.

## Commit body

- Add a body for every non-trivial commit. Separate it from the subject with one blank line.
- Wrap body text at 72 characters and use blank lines between paragraphs. Use bullet points when they make the message
  easier to understand.
- Explain what changed and why it changed. Leave implementation mechanics to the diff.
- Provide enough context for a reviewer to judge whether the change is appropriate without first reading the diff.
- Prefer this order when applicable:
  1. Describe the existing situation in the present tense.
  2. Explain why it needs to change.
  3. State what the commit does in the imperative mood.
  4. Explain why that approach was chosen.
  5. Add other relevant context.
- Avoid `currently` and `originally` when describing the existing situation because the timing is already implied.
- Do not repeat details already explained by comments in the same commit.
- If the message becomes overly long or covers unrelated reasons, split the work into finer-grained commits.

## Branch names

- Use a meaningful kebab-case name containing relevant keywords, for example, `refactor-ui-tests`.
- For a branch tied to an issue, use `<issue-number>-<keywords-from-issue-title>`, for example,
  `1234-ui-freeze-error`.

## Completion check

Before proposing or creating a commit, check the subject length, mood, capitalization, punctuation, and body formatting.
For a non-trivial commit, confirm that the body explains both what and why. Before suggesting or creating a branch,
check that its name follows the branch rules above.
