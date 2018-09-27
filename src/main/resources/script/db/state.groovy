package script.db

databaseChangeLog(logicalFilePath: 'script/db/state_machine.groovy') {

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-08-06-state') {
        createTable(tableName: 'state') {
            column(name: 'id', type: 'BIGINT UNSIGNED', autoIncrement: 'true', remarks: 'ID,主键') {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')
            column(name: 'type', type: 'CHAR(1)', remarks: '类型') {
                constraints(nullable: false)
            }
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state", indexName: "state_n1") {
            column(name: "name", type: "VARCHAR(64)")
        }
        createIndex(tableName: "state", indexName: "state_n2") {
            column(name: "description", type: "VARCHAR(255)")
        }
        createIndex(tableName: "state", indexName: "state_n3") {
            column(name: "type", type: "CHAR(1)")
        }
        createIndex(tableName: "state", indexName: "state_n4") {
            column(name: "organization_id", type: "BIGINT UNSIGNED")
        }
    }
}