# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added
- Generate reports on the messages on the DLQ

## [3.2.0] - 2019-02-11

### Added
- Output of consumer name so that it's easy to see where the DLQ message was going to

## [3.1.0] - 2018-10-30

### Added
- Added capability to submit a _`reprocess all`_ command to the Artemis broker, to retry all current messages in the DLQ. 

## [3.0.0] - 2018-09-06

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
