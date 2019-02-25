package io.choerodon.statemachine.infra.utils;

public final class SagaTopic {

    private SagaTopic() {
    }
    public static class Project {
        private Project() {
        }

        //创建项目
        public static final String PROJECT_CREATE = "iam-create-project";
        //更新项目
        public static final String PROJECT_UPDATE = "iam-update-project";
        //停用项目
        public static final String PROJECT_DISABLE = "iam-disable-project";
        //启用项目
        public static final String PROJECT_ENABLE = "iam-enable-project";
    }
    public static class Organization {
        private Organization() {
        }

        //组织服务创建组织
        public static final String ORG_CREATE = "org-create-organization";

        //iam接收创建组织事件的SagaTaskCode
        public static final String TASK_ORG_CREATE = "statemachine-create-organization";
    }


}
