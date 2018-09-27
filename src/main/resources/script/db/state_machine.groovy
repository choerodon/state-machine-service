package script.db

databaseChangeLog(logicalFilePath: 'script/db/state_machine.groovy') {
    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-07-30-state-machine') {
        createTable(tableName: 'state_machine') {
            column(name: 'id', type: 'BIGINT UNSIGNED', autoIncrement: 'true', remarks: 'ID,主键') {
                constraints(primaryKey: 'true')
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: 'false')
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')
            column(name: 'status', type: 'CHAR(1)', remarks: '状态', defaultValue: "0")
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织id') {
                constraints(nullable: 'false')
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state_machine", indexName: "state_machine_n1") {
            column(name: "name", type: "VARCHAR(64)")
        }
        createIndex(tableName: "state_machine", indexName: "state_machine_n2") {
            column(name: "status", type: "CHAR(1)")
        }
        createIndex(tableName: "state_machine", indexName: "state_machine_n3") {
            column(name: "organization_id", type: "BIGINT UNSIGNED")
        }
    }

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

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-07-30-state-machine-transf') {
        createTable(tableName: 'state_machine_transf') {
            column(name: 'id', type: 'BIGINT UNSIGNED', autoIncrement: 'true', remarks: 'ID,主键') {
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
            column(name: "is_all_state_transf", type: "TYNIINT UNSIGNED", defaultValue: '0', remarks: '所有状态都可以转换给当前状态')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: "state_machine_transf", indexName: "state_machine_transf_n1") {
            column(name: "state_machine_id", type: "BIGINT UNSIGNED")
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-07-30-state-machine-node-deploy') {
        createTable(tableName: 'state_machine_node_deploy') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID,主键') {
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
        createIndex(tableName: "state_machine_node_deploy", indexName: "state_machine_node_deploy_n1") {
            column(name: "state_machine_id", type: "BIGINT UNSIGNED")
        }
        createIndex(tableName: "state_machine_node_deploy", indexName: "state_machine_node_deploy_n2") {
            column(name: "state_id", type: "BIGINT UNSIGNED")
        }
    }

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
            column(name: "is_all_state_transf", type: "TYNIINT UNSIGNED", defaultValue: '0', remarks: '所有状态都可以转换给当前状态')

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

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-12-add-column-state-machine-transf') {
        addColumn(tableName: 'state_machine_transf') {
            column(name: 'style', type: 'clob', remarks: '样式')
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-12-add-column-state-machine-transf-deploy') {
        addColumn(tableName: 'state_machine_transf_deploy') {
            column(name: 'style', type: 'clob', remarks: '样式')
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-18-add-column-state-machine-transf') {
        addColumn(tableName: 'state_machine_transf') {
            column(name: 'condition_strategy', type: 'varchar(20)', remarks: '条件策略', defaultValue: "all")
        }
    }

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-18-add-column-state-machine-transf-deploy') {
        addColumn(tableName: 'state_machine_transf_deploy') {
            column(name: 'condition_strategy', type: 'varchar(20)', remarks: '条件策略', defaultValue: "all")
        }
    }

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

    changeSet(author: 'peng.jiang@hand-china.com', id: '2018-09-12-state-machine-config-deploy') {
        createTable(tableName: 'state_machine_config_deploy') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'ID,主键') {
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
        createIndex(tableName: "state_machine_config_deploy", indexName: "state_machine_config_deploy_n1") {
            column(name: "transf_id", type: "BIGINT UNSIGNED")
        }
        createIndex(tableName: "state_machine_config_deploy", indexName: "state_machine_config_deploy_n2") {
            column(name: "type", type: "VARCHAR(255)")
        }
    }

}