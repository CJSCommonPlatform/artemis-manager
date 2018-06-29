# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

## [2.1.0] - 2018-06-29

###Added
- Output of consumer name so that it's easy to see where the DLQ message was going to

## [2.0.0] - 2017-0-15

### Fixed
- Browse fails with exception if browsing large messages.  Now uses JMS QueueBrowser instead of JMX for browsing of messages.

## [1.0.0] - 2017-01-06

### Added
- Initial release of the artemis-manager tool, supporting browse remove and reprocess a message operations on the DLQ.
