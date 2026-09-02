---
name: seedu-java-coding-standard
description: Apply and review the SE-EDU basic and intermediate Java coding standard whenever creating, modifying, or reviewing handwritten Java source or test code in this project.
---

# SE-EDU Java Coding Standard

Follow the [SE-EDU Java coding standard (basic + intermediate)](https://se-education.org/guides/conventions/java/intermediate.html).
For topics it does not cover, follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
Preserve program behavior when making style-only changes.

## Naming

- Put every class in a lowercase package. Use `alpha` as this project's root package, followed by logical lowercase
  subpackages when needed.
- Name classes and enums with PascalCase nouns, variables with camelCase nouns, methods with camelCase verbs, and
  constants with SCREAMING_SNAKE_CASE.
- In test method names, use `featureUnderTest_testScenario_expectedBehavior`; omit later parts only when the test's
  scope makes them unnecessary.
- Treat abbreviations and acronyms as words inside names, such as `exportHtmlSource`, not `exportHTMLSource`.
- Use English names. Give wider-scope variables more descriptive names; reserve short scratch names such as `i` and
  `j` for small scopes and nested loop indices.
- Make boolean names read as booleans, preferably with prefixes such as `is`, `has`, `was`, `can`, or `should`.
  Name a boolean setter parameter after the property, for example `setFound(boolean isFound)`.
- Use plural names for collections and arrays. Give associated constants a common prefix.

## Layout

- Indent with four spaces, never tabs. Use eight additional spaces for wrapped continuation lines.
- Keep lines below 110 characters where practical and never exceed 120 characters.
- When wrapping, prioritize readability: break after commas and before operators, including `.`, `&`, and `|`.
  Keep a method or constructor name attached to its opening parenthesis and prefer higher-level breaks.
- Use K&R braces. Put braces around every loop and conditional body, including single statements, and put the body
  on its own line.
- Put spaces around operators, after Java keywords and commas, around ternary colons, and after semicolons in `for`
  headers. Separate logical units within a block with one blank line.
- Format methods, conditionals, loops, `switch`, and `try` statements as shown in the source standard. Mark intentional
  traditional-switch fall-through with `// Fallthrough`.

## Statements and declarations

- List imports explicitly; never use wildcard imports. Keep import ordering consistent and remove unused imports.
- Attach array brackets to the type, for example `int[] values`.
- Initialize variables when declared and declare them in the smallest practical scope. Leave a variable uninitialized
  rather than assigning a fake value when no valid initial value exists.
- Do not expose mutable class variables as `public` unless the class is a behavior-free data class. Constants are
  exempt.

## Comments and Javadoc

- Write comments in English using American spelling and no local slang. Indent comments with the code they describe.
- Add descriptive Javadoc to every class and public method. It may be omitted for getters/setters, tests, and exact
  overrides whose inherited Javadoc fully applies.
- Start Javadoc with a concise summary sentence using third-person wording such as `Returns`, `Adds`, or `Sends`.
  Put `/**` on its own line, align each `*`, leave one blank starred line before block tags, punctuate tag descriptions,
  and place no blank line between the Javadoc and declaration.
- Include either all useful `@param` tags or none. Omit `@return` for `void` methods or when the return value is already
  obvious from the description. Use `{@inheritDoc}` when inherited documentation needs an extension.

## Completion check

Before finishing a Java change, inspect every changed Java file for these rules. Run the project's available formatter,
style checks, and tests with Java 25 when available; otherwise report which verification could not be performed.
