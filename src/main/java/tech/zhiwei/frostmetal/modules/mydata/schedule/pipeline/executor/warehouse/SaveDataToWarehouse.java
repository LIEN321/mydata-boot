package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 保存业务数据到数据仓库
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class SaveDataToWarehouse extends TaskExecutor {
    private final IDataFieldService dataFieldService = SpringUtil.getBean(IDataFieldService.class);
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
    private final IBizDataService bizDataService = SpringUtil.getBean(IBizDataService.class);

    public SaveDataToWarehouse(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 输入参数
        Map<String, String> inputMap = getInputMap();

        // 获业务数据的key
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("业务数据的变量名为空，结束执行。");
            throw new IllegalArgumentException("业务数据的变量名为空，结束执行。");
        }

        // 业务数据集合
        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(bizDataKey);
        if (CollectionUtil.isEmpty(bizDataList)) {
            log("没有待保存的业务数据，结束执行。");
            return;
        }

        // 标准数据id
        Long dataId = (Long) jobContextData.get(MyDataConstant.JOB_DATA_KEY_DATA_ID);
        if (ObjectUtil.isNull(dataId)) {
            error("保存业务数据失败：前置任务中没有选择标准数据");
            throw new RuntimeException("保存业务数据失败：前置任务中没有选择标准数据");
        }

        // 标准数据字段列表
        List<DataField> dataFields = dataFieldService.listByData(dataId);
        if (CollectionUtil.isEmpty(dataFields)) {
            error("保存业务数据失败：标准数据没有字段");
            throw new RuntimeException("保存业务数据失败：标准数据没有字段");
        }

        // 从字段列表提取标识字段
        List<DataField> idFields = dataFields.stream().filter(DataField::getIsId).toList();
        if (CollectionUtil.isEmpty(idFields)) {
            error("保存业务数据失败：标准数据没有标识字段");
            throw new RuntimeException("保存业务数据失败：标准数据没有标识字段");
        }

        // 保存数据到数据中心
        List<Map<String, Object>> dataInsertList = CollectionUtil.newArrayList();
        List<Map<String, Object>> dataUpdateList = CollectionUtil.newArrayList();
        // 没有变化的数据量
        AtomicInteger sameCount = new AtomicInteger();

        // 实际入库的业务数据
        List<Map<String, Object>> savedDataList = CollectionUtil.newArrayList();

        // 当前流水线
        PipelineTask pipelineTask = getPipelineTask();
        // 所属项目
        Project project = MyDataCache.getProject(pipelineTask.getProjectId());
        // 数据仓库名称
        String warehouseName = MyDataUtil.getBizDbCode(pipelineTask.getTenantId(), project.getProjectCode());
        // 标准数据的编号
        String dataCode = jobContextData.get(MyDataConstant.JOB_DATA_KEY_DATA_CODE).toString();

        // 遍历业务数据
        bizDataList.forEach(bizData -> {
            // 标识字段 键值对
            Map<String, Object> idMap = MapUtil.newHashMap();
            for (DataField idField : idFields) {
                String idCode = idField.getFieldCode();
                Object idFieldValue = bizData.get(idCode);
                idMap.put(idCode, idFieldValue);
            }

            // 根据唯一标识 查询业务数据
            Map<String, Object> queryData = bizDataDAO.findByIds(warehouseName, dataCode, idMap);

            if (queryData == null) {
                // 未查到数据，则新增
                queryData = bizData;
                // 存入待新增列表
                dataInsertList.add(queryData);
            } else {
                // 查到数据
                // 检测数据 是否需要变更，若有则更新 否则不更新
                boolean isSame = true;
                Set<String> keys = bizData.keySet();
                for (String key : keys) {
                    Object produceDataValue = bizData.get(key);
                    Object queryDataValue = queryData.get(key);

                    // TODO 将保存的数据 按最新配置的类型转换对比
//                    String targetType = taskJob.getFieldTypeMapping().get(key);
//                    produceDataValue = MdUtil.convertDataType(produceDataValue, targetType);
//                    queryDataValue = MdUtil.convertDataType(queryDataValue, targetType);
                    if (!ObjectUtil.equal(produceDataValue, queryDataValue)) {
                        isSame = false;
                        break;
                    }
                }
                if (isSame) {
                    sameCount.getAndIncrement();
                    return;
                }

                // 将业务数据 覆盖更新 查询的数据
                queryData.putAll(bizData);
                // 存入待更新列表
                dataUpdateList.add(queryData);
            }
        });

        // 新增数据 到 数据仓库
        if (!dataInsertList.isEmpty()) {
            bizDataDAO.insertBatch(warehouseName, dataCode, dataInsertList);
            savedDataList.addAll(dataInsertList);
            log("新增数据 {} 条", dataInsertList.size());
        } else {
            log("无新增数据");
        }

        // 更新数据仓库的数据
        if (!dataUpdateList.isEmpty()) {
            dataUpdateList.forEach(data -> {
                Map<String, Object> idMap = MapUtil.newHashMap();
                for (DataField idField : idFields) {
                    String idCode = idField.getFieldCode();
                    Object dataIdValue = data.get(idCode);
                    idMap.put(idCode, dataIdValue);
                }

                bizDataDAO.update(warehouseName, dataCode, idMap, data);
            });
            savedDataList.addAll(dataUpdateList);

            log("更新数据 {} 条", dataUpdateList.size());
        } else {
            log("无更新数据");
        }

        log("实际保存数据 {} 条，没有变化的数据 {} 条", savedDataList.size(), sameCount.get());

        // 输出参数
        Map<String, String> outputMap = getOutputMap();
        String savedDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_SAVED_DATA);
        if (StringUtil.isNotEmpty(savedDataKey)) {
            jobContextData.put(savedDataKey, savedDataList);
        }

        bizDataService.updateDataCount(dataId);
        log("更新标准数据的业务数量");
    }
}
