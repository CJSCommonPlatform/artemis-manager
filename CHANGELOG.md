# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- Added capability to query queue names over JMX
- Added capability to query topic names over JMX
- Added capability to query queue messageCount over JMX
- Added capability to query topic messageCount over JMX

### Changed
- imported framework-command-cli directly as its no longer supported

## [2.0.0] - 2017-0-15

### Fixed
- Browse fails with exception if browsing large messages.  Now uses JMS QueueBrowser instead of JMX for browsing of messages.

## [1.0.0] - 2017-01-06

### Added
- Initial release of the artemis-manager tool, supporting browse remove and reprocess a message operations on the DLQ.
