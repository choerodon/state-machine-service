# Changelog
All notable changes about state-machine-service will be documented in this file.

## [0.13.0] - 2019-01-11

### Changed  

- The optimization of state-machine canvas and style.

### Fixed

- Agile service add status, from the state-machine node configuration with deploy and draft at the same time to add, and repair the dirty data.
- Agile service delete status, from the state-machine node configuration with deploy and draft at the same time to remove, and repair the dirty data.


## [0.12.0] - 2018-12-14

### Added

- The state-machine instance recovery strategy, avoid the instance of the consumption of resources
- Added all the unit tests.

### Changed  

- Optimize operation flow chart of the state-machine.
- The state-machine instance flow performance optimization.


## [0.11.0] - 2018-11-16

### Added

- Organization level add status maintenance
- Organization level add state-machine maintenance
- The state-machine configuration add all-transform, configure all the transform of node, all nodes can be transformed to the current node, apply for agile.

### Fixed

- Migration on the status of agile/test project to the Organization level.
