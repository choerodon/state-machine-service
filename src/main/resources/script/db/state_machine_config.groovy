package script.db

databaseChangeLog(logicalFilePath: 'script/db/state_machine.groovy') {

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-12-state-machine-config') {
        createTable(tableName: 'state_machine_config') {
            column(name: 'id', type: 'BIGINT UNSIGNED', autoIncrement: 'true', remarks: 'ID,主键') {
                constraints(primaryKey: 'true')
            }
            column(name: 'transf_id', type: 'BIGINT UNSIGNED', remarks: '转换id') {
                constraints(nullable: 'false')
            }
            column(name: 'state_machine_id', type: 'BIGINT UNSIGNED', remarks: '状态机id') {
                constraints(nullable: 'false')
            }
            column(name: 'code', type: 'VARCHAR(255)', remarks: '编码') {
                constraints(nullable: 'false')
            }
            column(name: 'type', type: 'VARCHAR(255)', remarks: '类型') {
                constraints(nullable: 'false')
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state_machine_config", indexName: "state_machine_config_n1") {
            column(name: "transf_id", type: "BIGINT UNSIGNED")
        }
        createIndex(tableName: "state_machine_config", indexName: "state_machine_config_n2") {
            column(name: "type", type: "VARCHAR(255)")
        }
    }

}