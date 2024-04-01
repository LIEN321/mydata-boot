package org.springblade.modules.mydata.job.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务的数据处理类
 *
 * @author LIEN
 * @since 2022/7/16
 */
@Component
public class JobDataService {

    @Resource
    private BizDataDAO bizDataDAO;

    /**
     * 根据任务配置，从json中解析出业务数据列表
     *
     * @param taskInfo   任务
     * @param jsonString json字符串
     */
    public void parseData(TaskInfo taskInfo, String jsonString) {
        // 获取任务中的字段映射配置
        Map<String, String> fieldMapping = taskInfo.getFieldMapping();
        // 映射字段的类型
        Map<String, String> mappingFieldType = taskInfo.getMappingFieldType();
        if (CollUtil.isEmpty(fieldMapping)) {
            taskInfo.appendLog("任务没有配置字段映射，跳过解析业务数据");
            return;
        }

        // 字段层级前缀
        String apiFieldPrefix = taskInfo.getApiFieldPrefix();

        JSON json = JSONUtil.parse(jsonString);
        if (StrUtil.isNotEmpty(apiFieldPrefix)) {
            Object prefixJson = json.getByPath(apiFieldPrefix);
            if (!(prefixJson instanceof JSON)) {
                throw new RuntimeException("接口前缀 无法解析为JSON");
            }
            json = (JSON) prefixJson;
        }
        JSONArray jsonArray;
        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        } else {
            jsonArray = new JSONArray();
            jsonArray.add(json);
        }

        // 声明方法返回结果
        List<Map> apiResponseDataList = CollUtil.newArrayList();

        // 根据映射 解析出json中的数据 并存入数据
        jsonArray.forEach(obj -> {
            JSONObject jsonObject = (JSONObject) obj;
            Map<String, Object> datacenterData = MapUtil.newHashMap();
            fieldMapping.forEach((standardCode, apiCode) -> {
                // 若字段映射中 未设置api参数名，则跳过处理；
                if (StrUtil.isEmpty(apiCode)) {
                    return;
                }
                datacenterData.put(standardCode, getValue(mappingFieldType, standardCode, jsonObject.get(apiCode)));
            });

            apiResponseDataList.add(datacenterData);
        });

        taskInfo.setProduceDataList(apiResponseDataList);
        taskInfo.appendLog("解析前json数据：{}", jsonString);
        taskInfo.appendLog("解析后业务数据：{}", apiResponseDataList);
    }

    /**
     * 根据任务中字段映射，将consumeDataList转换为api参数结构
     *
     * @param taskInfo 任务
     */
    public void convertData(TaskInfo taskInfo) {
        List<Map> consumeDataList = taskInfo.getConsumeDataList();
        // 获取任务的数据映射
        // 映射中，key为数据中心字段名，value为api字段名
        Map<String, String> mFieldMapping = taskInfo.getFieldMapping();
        Assert.notEmpty(mFieldMapping, "任务未设置数据映射");

        // 遍历数据中心数据，根据映射 转换为接口结构的数据
        List<Map> apiRequestDataList = CollUtil.newArrayList();

        consumeDataList.forEach(data -> {
            Map<String, Object> apiData = MapUtil.newHashMap();
            // 根据映射关系 将数据转换为api的数据结构
            mFieldMapping.forEach((standardCode, apiCode) -> {
                // 若字段映射中 未设置api参数名，则跳过处理；
                if (StrUtil.isEmpty(apiCode)) {
                    return;
                }
                apiData.put(apiCode, data.get(standardCode));
            });

            apiRequestDataList.add(apiData);
        });

        taskInfo.setConsumeDataList(apiRequestDataList);
    }

    public void saveTaskData(TaskInfo task) {
        Assert.notNull(task);
//        Assert.notEmpty(task.getProduceDataList(), "error: 保存数据到仓库失败，task.datas是空的");
        if (CollUtil.isEmpty(task.getProduceDataList())) {
            task.appendLog("任务中没有业务数据，跳过保存操作");
            return;
        }

        final Date currentTime = DateUtil.date();

        // 标准数据编号
        String dataCode = task.getDataCode();
        // 数据的标识字段编号
        String dataIdCode = task.getIdFieldCode();
        List<String> dataIdCodes = StrUtil.split(dataIdCode, StrPool.COMMA);

        // 保存数据到数据中心
        List<Map<String, Object>> dataInsertList = CollUtil.newArrayList();
        List<Map<String, Object>> dataUpdateList = CollUtil.newArrayList();
        task.getProduceDataList().forEach(standardDataValue -> {

            Map<String, Object> idMap = MapUtil.newHashMap();
            // 若数据的 标识字段值 无效，则不存储
            for (String idCode : dataIdCodes) {
                Object idFieldValue = standardDataValue.get(idCode);
                if (ObjectUtil.isNull(idFieldValue)) {
                    return;
                }

                idMap.put(idCode, idFieldValue);
            }

            // 根据唯一标识 查询业务数据
            Map<String, Object> value = bizDataDAO.findByIds(MdUtil.getBizDbCode(task.getTenantId(), task.getProjectId(), task.getEnvId()), task.getDataCode(), idMap);

            if (value == null) {
                // 未查到数据，则新增
                value = standardDataValue;
                dataInsertList.add(value);
            } else {
                // 查到数据，则更新
                value.putAll(standardDataValue);
                dataUpdateList.add(value);
            }

            // 设置业务数据的最后更新时间
            value.put(MdConstant.DATA_COLUMN_UPDATE_TIME, currentTime);
        });

        // 新增数据 到 数据仓库
        if (!dataInsertList.isEmpty()) {
            bizDataDAO.insertBatch(MdUtil.getBizDbCode(task.getTenantId(), task.getProjectId(), task.getEnvId()), dataCode, dataInsertList);
        }

        // 更新数据仓库的数据
        if (!dataUpdateList.isEmpty()) {
            dataUpdateList.forEach(data -> {
                Map<String, Object> idMap = MapUtil.newHashMap();
                dataIdCodes.forEach(idCode -> {
                    Object dataIdValue = data.get(idCode);
                    idMap.put(idCode, dataIdValue);
                });

                bizDataDAO.update(MdUtil.getBizDbCode(task.getTenantId(), task.getProjectId(), task.getEnvId()), dataCode, idMap, data);
            });
        }

        // 更新业务数据量
        // v0.7.0 取消，该字段由于数据按环境区分存储而失效
        // dataService.updateDataCount(task.getTenantId(), task.getDataId());

        task.appendLog("保存业务数据，新增：{}，更新：{}", dataInsertList, dataUpdateList);
    }

    /**
     * 根据字段类型配置，将接口数据 转为指定类型
     *
     * @param mappingFieldType 映射字段类型
     * @param fieldCode        映射字段
     * @param apiValue         接口数据
     * @return 转换后的数据
     */
    private Object getValue(Map<String, String> mappingFieldType, String fieldCode, Object apiValue) {
        Object value = apiValue;
        if (CollUtil.isNotEmpty(mappingFieldType)) {
            String fieldType = mappingFieldType.get(fieldCode);
            if (StrUtil.isNotEmpty(fieldType)) {
                switch (fieldType) {
                    case "int":
                        value = NumberUtil.parseInt(StrUtil.toString(apiValue));
                        break;
                    case "string":
                        value = StrUtil.toString(apiValue);
                        break;
                    case "date":
                        value = DateUtil.parse(StrUtil.toString(apiValue));
                        break;
                }
            }
        }

        return value;
    }
}
