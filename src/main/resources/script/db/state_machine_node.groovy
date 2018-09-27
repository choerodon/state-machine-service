package script.db

databaseChangeLog(logicalFilePath: 'script/db/state_machine.groovy') {

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-07-30-state-machine-node') {
        createTable(tableName: 'state_machine_node') {
            column(name: 'id', type: 'BIGINT UNSIGNED', autoIncrement: 'true', remarks: 'ID,主键') {
                constraints(primaryKey: 'true')
            }
            column(name: 'state_machine_id', type: 'BIGINT UNSIGNED', remarks: '状态机id') {
                constraints(nullable: 'false')
            }
            column(name: 'state_id', type: 'BIGINT UNSIGNED', remarks: '状态id') {
                constraints(nullable: 'false')
            }
            column(name: 'position_x', type: 'BIGINT', remarks: '坐标x')
            column(name: 'position_y', type: 'BIGINT', remarks: '坐标y')
            column(name: 'width', type: 'BIGINT UNSIGNED', remarks: '宽')
            column(name: 'height', type: 'BIGINT UNSIGNED', remarks: '高')
            column(name: 'status', type: 'CHAR(1)', remarks: '节点状态类型', defaultValue: '0')
            column(name: "all_state_transf_id", type: "BIGINT UNSIGNED", defaultValue: '0', remarks: '所有状态都可以转换给当前状态的转换id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state_machine_node", indexName: "state_machine_node_n1") {
            column(name: "state_machine_id", type: "BIGINT UNSIGNED")
        }
        createIndex(tableName: "state_machine_node", indexName: "state_machine_node_n2") {
            column(name: "state_id", type: "BIGINT UNSIGNED")
        }
    }

}