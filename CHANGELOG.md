# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning].

## Unreleased

### Security

- Stop writing every exported shadowenv variable and its value to the IDE log at WARN level. Exported values can be secrets, and the IDE log is kept on disk and attached to bug reports.

## 0.0.1 - 2019-08-24

### Added

- Intellij IDEA and Rubymine integrations

