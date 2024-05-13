package org.springblade.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.manage.entity.*;
import org.springblade.modules.mydata.manage.mapper.*;
import org.springblade.modules.mydata.manage.service.IMyDataService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * mydata 通用业务实现类
 *
 * @author LIEN
 * @since 2024/5/13
 */
@Service
@AllArgsConstructor
public class MyDataService implements IMyDataService {

    private ProjectMapper projectMapper;
    private EnvMapper envMapper;
    private AppMapper appMapper;
    private ApiMapper apiMapper;
    private DataMapper dataMapper;
    private DataFieldMapper dataFieldMapper;
    private TaskMapper taskMapper;

    @Override
    public void initData(String tenantId) {
        // 1个默认项目
        Project project = new Project();
        project.setProjectCode("demo_project");
        project.setProjectName("项目（示例）");
        project.setTenantId(tenantId);
        projectMapper.insert(project);

        // 1个环境
        Env env = new Env();
        env.setEnvName("默认环境");
        env.setEnvPrefix("");
        env.setProjectId(project.getId());
        env.setSort(1);
        env.setTenantId(tenantId);
        envMapper.insert(env);

        // 1个示例应用
        App app = new App();
        app.setAppCode(("demo_app"));
        app.setAppName("应用（示例）");
        app.setTenantId(tenantId);
        appMapper.insert(app);

        // 2个示例API, GET、POST
        Api produceApi = new Api();
        produceApi.setApiName("提供用户数据");
        produceApi.setOpType(MdConstant.DATA_PRODUCER);
        produceApi.setApiMethod(MdConstant.HttpMethod.GET.toString());
        produceApi.setApiUri("https://test.mydata.work/mydata-test/test/hr_user");
        produceApi.setDataType(MdConstant.ApiDataType.JSON.toString());
        produceApi.setAppId(app.getId());
        produceApi.setFieldPrefix("data");
        produceApi.setTenantId(tenantId);
        apiMapper.insert(produceApi);

        Api consumeApi = new Api();
        consumeApi.setApiName("消费用户数据");
        consumeApi.setOpType(MdConstant.DATA_CONSUMER);
        consumeApi.setApiMethod(MdConstant.HttpMethod.POST.toString());
        consumeApi.setApiUri("https://test.mydata.work/mydata-test/test/save_oa_user");
        consumeApi.setDataType(MdConstant.ApiDataType.JSON.toString());
        consumeApi.setAppId(app.getId());
        consumeApi.setTenantId(tenantId);
        apiMapper.insert(consumeApi);

        // 1个示例数据
        Data data = new Data();
        data.setDataCode("user");
        data.setDataName("用户");
        data.setProjectId(project.getId());
        data.setTenantId(tenantId);
        dataMapper.insert(data);

        DataField field1 = new DataField();
        field1.setDataId(data.getId());
        field1.setFieldCode("user_code");
        field1.setFieldName("编号");
        field1.setIsId(1);
        field1.setFieldType(MdConstant.DATA_TYPE_DEFAULT);
        field1.setTenantId(tenantId);
        dataFieldMapper.insert(field1);

        DataField field2 = new DataField();
        field2.setDataId(data.getId());
        field2.setFieldCode("user_name");
        field2.setFieldName("姓名");
        field2.setIsId(0);
        field2.setFieldType(MdConstant.DATA_TYPE_STRING);
        field2.setTenantId(tenantId);
        dataFieldMapper.insert(field2);

        DataField field3 = new DataField();
        field3.setDataId(data.getId());
        field3.setFieldCode("birthday");
        field3.setFieldName("出生日期");
        field3.setIsId(0);
        field3.setFieldType(MdConstant.DATA_TYPE_DATE);
        field3.setTenantId(tenantId);
        dataFieldMapper.insert(field3);

        DataField field4 = new DataField();
        field4.setDataId(data.getId());
        field4.setFieldCode("age");
        field4.setFieldName("年龄");
        field4.setIsId(0);
        field4.setFieldType(MdConstant.DATA_TYPE_INT);
        field4.setTenantId(tenantId);
        dataFieldMapper.insert(field4);

        DataField field5 = new DataField();
        field5.setDataId(data.getId());
        field5.setFieldCode("salary");
        field5.setFieldName("工资");
        field5.setIsId(0);
        field5.setFieldType(MdConstant.DATA_TYPE_NUMBER);
        field5.setTenantId(tenantId);
        dataFieldMapper.insert(field5);

        // 2个示例任务
        Task task1 = new Task();
        task1.setProjectId(project.getId());
        task1.setEnvId(env.getId());
        task1.setDataId(data.getId());
        task1.setOpType(produceApi.getOpType());
        task1.setTaskName("定时获取用户数据");
        task1.setAppId(app.getId());
        task1.setProduceMode(MdConstant.TASK_PRODUCE_MODE_API);
        task1.setApiId(produceApi.getId());
        task1.setIsSubscribed(MdConstant.TASK_IS_NOT_SUBSCRIBED);
        task1.setTaskPeriod("0 0/1 * * * ?");
        LinkedHashMap<String, String> fieldMapping = new LinkedHashMap<>();
        fieldMapping.put("user_code", "userCode");
        fieldMapping.put("user_name", "userName");
        fieldMapping.put("birthday", "birthday");
        fieldMapping.put("age", "age");
        fieldMapping.put("salary", "salary");
        task1.setFieldMapping(fieldMapping);
        task1.setDataCode(data.getDataCode());
        task1.setIdFieldCode(field1.getFieldCode());
        task1.setApiMethod(produceApi.getApiMethod());
        task1.setDataType(produceApi.getDataType());
        task1.setApiUrl(produceApi.getApiUri());
        task1.setApiFieldPrefix(produceApi.getFieldPrefix());
        task1.setTaskStatus(MdConstant.TASK_STATUS_STOPPED);
        task1.setTenantId(tenantId);
        taskMapper.insert(task1);

        Task task2 = new Task();
        task2.setProjectId(project.getId());
        task2.setEnvId(env.getId());
        task2.setDataId(data.getId());
        task2.setOpType(consumeApi.getOpType());
        task2.setTaskName("定时消费用户数据");
        task2.setAppId(app.getId());
        task2.setProduceMode(MdConstant.TASK_CONSUME_MODE_API);
        task2.setApiId(consumeApi.getId());
        task2.setIsSubscribed(MdConstant.TASK_IS_NOT_SUBSCRIBED);
        task2.setTaskPeriod("0 0/1 * * * ?");
        task2.setFieldMapping(fieldMapping);
        task2.setDataCode(data.getDataCode());
        task2.setIdFieldCode(field1.getFieldCode());
        task2.setApiMethod(consumeApi.getApiMethod());
        task2.setDataType(consumeApi.getDataType());
        task2.setApiUrl(consumeApi.getApiUri());
        task2.setApiFieldPrefix(consumeApi.getFieldPrefix());
        task2.setTaskStatus(MdConstant.TASK_STATUS_STOPPED);
        task2.setTenantId(tenantId);
        taskMapper.insert(task2);
    }
}

