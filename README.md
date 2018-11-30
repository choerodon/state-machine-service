# State Machine Service
`State Machine Service` is the core service of Choerodon.  

The service is responsible for controlling the flow of the state, and is driven by the state machine to set the conditions, verification, and post-action of the state transition, combined with the state machine client and the solution of the concern.

## Features
- **Status Management**
- **State Machine Management**

## Requirements
- Java8
- [Issue Service](https://github.com/choerodon/issue-service.git)
- [Agile Service](https://github.com/choerodon/agile-service.git)
- [Iam Service](https://github.com/choerodon/iam-service.git)
- [MySQL](https://www.mysql.com)
- [Spring State Machine](https://projects.spring.io/spring-statemachine)

## Installation and Getting Started
1. init database

    ```sql
    CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
    CREATE DATABASE state_machine_service DEFAULT CHARACTER SET utf8;
    GRANT ALL PRIVILEGES ON state_machine_service.* TO choerodon@'%';
    FLUSH PRIVILEGES;
    ```
1. run command `sh init-local-database.sh`
1. run command as follow or run `StateMachineServiceApplication` in IntelliJ IDEA

    ```bash
    mvn clean spring-boot:run
    ```

## Dependencies
- `go-register-server`: Register server
- `iam-service`：iam service
- `mysql`: agile_service database
- `api-gateway`: api gateway server
- `gateway-helper`: gateway helper server
- `oauth-server`: oauth server
- `manager-service`: manager service
- `asgard-service`: asgard service
- `issue-service`: issue service
- `agile-service`: agile service

## Reporting Issues
If you find any shortcomings or bugs, please describe them in the  [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute
Pull requests are welcome! [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.
