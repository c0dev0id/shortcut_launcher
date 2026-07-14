# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- First-launch action-type menu, so the configured action is no longer limited
  to the system shortcut picker.
- "App Activity" action type: pick an installed app and name an activity class
  to save an explicit-component launch intent (useful for launcher-inaccessible
  activities such as an app's settings/manager screen).
- Nightly release workflow that builds signed release APKs and publishes them as
  a rolling `nightly` pre-release on every push to `main`.
