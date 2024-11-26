package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 保存业务数据到数据仓库
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class SaveDataToWarehouse extends TaskExecutor {
    private final IDataFieldService dataFieldService = SpringUtil.getBean(IDataFieldService.class);
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);

    public SaveDataToWarehouse(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public void execute(Map<String, Object> jobContextData) {
        // 业务数据集合
        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (CollectionUtil.isEmpty(bizDataList)) {
            // TODO 业务数据集合为空
            return;
        }

        PipelineTask pipelineTask = getPipelineTask();
        Project project = MyDataCache.getProject(pipelineTask.getProjectId());

        // 数据仓库名称
        String warehouseName = pipelineTask.getTenantId() + "_" + project.getProjectCode();
        // 标准数据的编号
        String dataCode = jobContextData.get(MyDataConstant.JOB_DATA_KEY_DATA_CODE).toString();

        // 标准数据id
        Long dataId = (Long) jobContextData.get(MyDataConstant.JOB_DATA_KEY_DATA_ID);
        if (ObjectUtil.isNull(dataId)) {
            throw new RuntimeException("保存业务数据失败：缺少标准数据");
        }

        // 标准数据字段列表
        List<DataField> dataFields = dataFieldService.listByData(dataId);
        if (CollectionUtil.isEmpty(dataFields)) {
            throw new RuntimeException("保存业务数据失败：标准数据没有字段");
        }

        // 从字段列表提取标识字段
        List<DataField> idFields = dataFields.stream().filter(DataField::getIsId).toList();
        if (CollectionUtil.isEmpty(idFields)) {
            throw new RuntimeException("保存业务数据失败：标准数据没有标识字段");
        }

        // 保存数据到数据中心
        List<Map<String, Object>> dataInsertList = CollectionUtil.newArrayList();
        List<Map<String, Object>> dataUpdateList = CollectionUtil.newArrayList();

        // 实际入库的业务数据
        List<Map<String, Object>> savedDataList = CollectionUtil.newArrayList();

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
        }
    }
}
