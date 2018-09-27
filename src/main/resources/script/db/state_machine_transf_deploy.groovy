package script.db

databaseChangeLog(logicalFilePath: 'script/db/state_machine_transf_deploy.groovy') {

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-07-30-state-machine-transf-deploy') {
        createTable(tableName: 'state_machine_transf_deploy') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID,主键') {
                constraints(primaryKey: 'true')
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称')
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')
            column(name: 'state_machine_id', type: 'BIGINT UNSIGNED', remarks: '状态机id') {
                constraints(nullable: 'false')
            }
            column(name: 'start_node_id', type: 'BIGINT UNSIGNED', remarks: '起始节点id') {
                constraints(nullable: 'false')
            }
            column(name: 'end_node_id', type: 'BIGINT UNSIGNED', remarks: '结束节点id') {
                constraints(nullable: 'false')
            }
            column(name: 'url', type: 'VARCHAR(255)', remarks: '页面')
            column(name: 'status', type: 'CHAR(1)', remarks: '转换状态类型', defaultValue: '0')
            column(name: "is_all_state_transf", type: "TINYINT UNSIGNED", defaultValue: '0', remarks: '所有状态都可以转换给当前状态')
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state_machine_transf_deploy", indexName: "state_machine_transf_deploy_n1") {
            column(name: "state_machine_id", type: "BIGINT UNSIGNED")
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-12-add-column-state-machine-transf-deploy') {
        addColumn(tableName: 'state_machine_transf_deploy') {
            column(name: 'style', type: 'clob', remarks: '样式')
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-18-add-column-state-machine-transf-deploy') {
        addColumn(tableName: 'state_machine_transf_deploy') {
            column(name: 'condition_strategy', type: 'varchar(20)', remarks: '条件策略', defaultValue: "all")
        }
    }

}