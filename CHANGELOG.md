# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning].

## 0.0.3 - 2026-11-23

### Security

- Stop writing every exported shadowenv variable and its value to the IDE log at WARN level. Exported values can be secrets, and the IDE log is kept on disk and attached to bug reports.
- Stop writing the run configuration's environment variables and their values to the IDE log (IDEA).

### Fixed

- Remove the variables that shadowenv unsets, instead of failing the run configuration with `ExecutionException: JsonNull`.
- Restore the build: it depended on Bintray, which is shut down.

## 0.0.2 - 2025-06-25

### Fixed

- Stop passing an empty argument to `shadowenv hook`.

## 0.0.1 - 2019-08-24

### Added

- Intellij IDEA and Rubymine integrations

