# State Machine Service
`State Machine Service` is the core service of Choerodon.  

The service is responsible for Issue management.

## Requirements
- Java8
- [Iam Service](https://github.com/choerodon/iam-service.git)
- [Issue Service](https://github.com/choerodon/issue-service.git)
- [MySQL](https://www.mysql.com)

## Installation and Getting Started
1. init database

    ```sql
    CREATE USER 'choerodon'@'%' IDENTIFIED BY "choerodon";
    CREATE DATABASE state_machine_service DEFAULT CHARACTER SET utf8;
    GRANT ALL PRIVILEGES ON state_machine_service.* TO choerodon@'%';
    FLUSH PRIVILEGES;
    ```
1. run command `sh init-local-database.sh`
1. run command as follow or run `AgileServiceApplication` in IntelliJ IDEA

    ```bash
    mvn clean spring-boot:run
    ```
