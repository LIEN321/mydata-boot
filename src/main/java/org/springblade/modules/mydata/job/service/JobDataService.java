package org.springblade.modules.mydata.job.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.apache.poi.ss.util.SheetUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.job.bean.TaskJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

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

    @Resource
    private JobDataProcessService jobDataProcessService;

    /**
     * 根据任务配置，从json中解析出业务数据列表
     *
     * @param taskJob    任务
     * @param jsonString json字符串
     */
    public void parseProduceData(TaskJob taskJob, String jsonString) {
        // 获取任务中的字段映射配置
        Map<String, String> fieldMapping = taskJob.getFieldMapping();
        // 映射字段的类型
        Map<String, String> fieldTypeMapping = taskJob.getFieldTypeMapping();
        if (CollUtil.isEmpty(fieldMapping)) {
            taskJob.appendLog("任务没有配置字段映射，跳过解析业务数据");
            return;
        }

        // 字段层级前缀
        String apiFieldPrefix = taskJob.getApiFieldPrefix();
        // 最初的json对象
        JSON originJson = JSONUtil.parse(jsonString);
        // 使用数组模式 兼容单个对象和数组模式
        JSONArray baseArray;
        if (originJson instanceof JSONArray) {
            baseArray = (JSONArray) originJson;
        } else {
            baseArray = new JSONArray();
            baseArray.add(originJson);
        }

        // 声明方法返回结果
        List<Map<String, Object>> produceDataList = CollUtil.newArrayList();

        baseArray.forEach(json -> {
            // 保留根目录json，用于 /field 格式提取数据
            JSON baseJson = (JSONObject) json;
            // 根据配置的prefix 定位到数据层级
            JSON dataJson = baseJson;
            if (StrUtil.isNotEmpty(apiFieldPrefix)) {
                Object prefixJson = baseJson.getByPath(apiFieldPrefix);
                if (!(prefixJson instanceof JSON)) {
                    throw new RuntimeException("接口前缀 无法解析为JSON");
                }
                dataJson = (JSON) prefixJson;
            }
            // 使用数组模式 兼容单个对象和数组模式
            JSONArray jsonArray;
            if (dataJson instanceof JSONArray) {
                jsonArray = (JSONArray) dataJson;
            } else {
                jsonArray = new JSONArray();
                jsonArray.add(dataJson);
            }

            // 根据映射 解析出json中的数据 并存入数据
            jsonArray.forEach(obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                Map<String, Object> produceData = MapUtil.newHashMap();
                fieldMapping.forEach((standardCode, apiCode) -> {
                    // 若字段映射中 未设置api参数名，则跳过处理；
                    if (StrUtil.isEmpty(apiCode)) {
                        return;
                    }

                    // 获取业务数据值
                    Object value;
                    // /field 根目录格式
                    if (StrUtil.startWith(apiCode, MdConstant.FIELD_MAPPING_ROOT)) {
                        value = baseJson.getByPath(apiCode.substring(MdConstant.FIELD_MAPPING_ROOT.length()));
                    } else {
                        value = jsonObject.getByPath(apiCode);
                    }
                    // 未获取到值，再解析属性表达式 从任务变量尝试获取数据
                    if (value == null && JobVarService.isFieldExp(apiCode)) {
                        value = JobVarService.parseDataFieldVar(apiCode, taskJob.getTaskVar(), taskJob.getFieldTypeMapping());
                    }
                    // 若接口数据中 没有执行的字段名，则跳过处理
                    if (value == null) {
                        return;
                    }

                    String targetType = fieldTypeMapping.get(standardCode);
                    try {
                        produceData.put(standardCode, MdUtil.convertDataType(value, targetType));
                    } catch (Exception e) {
                        taskJob.appendLog("转换业务数据出错，数据：{}，字段 {} 转为目标类型 {} 时出错：{}", obj, standardCode, targetType, e.getMessage());
                    }
                });

                produceDataList.add(produceData);
            });
        });

        taskJob.setProduceDataList(produceDataList);
    }

    /**
     * 查询消费的业务数据
     *
     * @param taskJob 任务
     * @param skip    跳过数量
     * @param limit   限制数量
     * @return 业务数据
     */
    public List<Map<String, Object>> listConsumeData(TaskJob taskJob, Long skip, Integer limit) {
        List<Map<String, Object>> dataList = bizDataDAO.list(MdUtil.getBizDbCode(taskJob.getTenantId(), taskJob.getProjectId(), taskJob.getEnvId()), taskJob.getDataCode(), taskJob.getDataFilters(), skip, limit);
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }

        dataList.forEach(consumeData -> {
            jobDataProcessService.processConsumeData(taskJob, consumeData);
        });

        return dataList;
    }

    /**
     * 根据任务中字段映射，将consumeDataList转换为api参数结构
     *
     * @param taskJob 任务
     */
    public void convertConsumeData(TaskJob taskJob) {
        List<Map<String, Object>> consumeDataList = taskJob.getConsumeDataList();
        // 获取任务的数据映射
        // 映射中，key为数据中心字段名，value为api字段名
        Map<String, String> mFieldMapping = taskJob.getFieldMapping();
        Assert.notEmpty(mFieldMapping, "任务未设置数据映射");

        // 遍历数据中心数据，根据映射 转换为接口结构的数据
        List<Map<String, Object>> apiDataList = CollUtil.newArrayList();

        consumeDataList.forEach(bizData -> {
            Map<String, Object> apiData = MapUtil.newHashMap();
            // 根据映射关系 将数据转换为api的数据结构
            mFieldMapping.forEach((standardCode, apiCode) -> {
                // 若字段映射中 未设置api参数名，则跳过处理；
                if (StrUtil.isEmpty(apiCode)) {
                    return;
                }
                apiData.put(apiCode, bizData.get(standardCode));
            });

            apiDataList.add(apiData);
        });

        taskJob.setConsumeDataList(apiDataList);
    }


    /**
     * 保存任务中的业务数据
     *
     * @param taskJob 任务
     */
    public void saveProduceData(TaskJob taskJob) {
        Assert.notNull(taskJob);
        //        Assert.notEmpty(task.getProduceDataList(), "error: 保存数据到仓库失败，task.datas是空的");
        if (CollUtil.isEmpty(taskJob.getProduceDataList())) {
            return;
        }

        final Date currentTime = DateUtil.date();

        // 标准数据编号
        String dataCode = taskJob.getDataCode();
        // 数据的标识字段编号
        String dataIdCode = taskJob.getIdFieldCode();
        List<String> dataIdCodes = StrUtil.split(dataIdCode, StrPool.COMMA);

        // 保存数据到数据中心
        List<Map<String, Object>> dataInsertList = CollUtil.newArrayList();
        List<Map<String, Object>> dataUpdateList = CollUtil.newArrayList();

        // 实际入库的业务数据
        List<Map<String, Object>> savedDataList = CollUtil.newArrayList();

        taskJob.getProduceDataList().forEach(produceData -> {

            // 标识字段 键值对
            Map<String, Object> idMap = MapUtil.newHashMap();
            for (String idCode : dataIdCodes) {
                Object idFieldValue = produceData.get(idCode);
                idMap.put(idCode, idFieldValue);
            }

            // 根据唯一标识 查询业务数据
            Map<String, Object> queryData = bizDataDAO.findByIds(MdUtil.getBizDbCode(taskJob.getTenantId(), taskJob.getProjectId(), taskJob.getEnvId()), taskJob.getDataCode(), idMap);

            // 根据字段映射配置 提前处理produceData数据，用于对比是否一致
            jobDataProcessService.processProduceData(taskJob, produceData, queryData);

            if (queryData == null) {
                // 未查到数据，则新增
                queryData = produceData;
                dataInsertList.add(queryData);
            } else {
                // 查到数据
                // 检测数据 是否需要变更，若有则更新 否则不更新
                boolean isSame = true;
                Set<String> keys = produceData.keySet();
                for (String key : keys) {
                    Object produceDataValue = produceData.get(key);
                    Object queryDataValue = queryData.get(key);

                    // 将保存的数据 按最新配置的类型转换对比
                    String targetType = taskJob.getFieldTypeMapping().get(key);
                    produceDataValue = MdUtil.convertDataType(produceDataValue, targetType);
                    queryDataValue = MdUtil.convertDataType(queryDataValue, targetType);
                    if (!ObjectUtil.equal(produceDataValue, queryDataValue)) {
                        isSame = false;
                        break;
                    }
                }
                if (isSame) {
                    return;
                }
                queryData.putAll(produceData);
                dataUpdateList.add(queryData);
            }

            queryData.put(MdConstant.DATA_COLUMN_BATCH_ID, taskJob.getDataBatchId());
        });

        // 新增数据 到 数据仓库
        if (!dataInsertList.isEmpty()) {
            bizDataDAO.insertBatch(MdUtil.getBizDbCode(taskJob.getTenantId(), taskJob.getProjectId(), taskJob.getEnvId()), dataCode, dataInsertList);
            savedDataList.addAll(dataInsertList);
        }

        // 更新数据仓库的数据
        if (!dataUpdateList.isEmpty()) {
            dataUpdateList.forEach(data -> {
                Map<String, Object> idMap = MapUtil.newHashMap();
                dataIdCodes.forEach(idCode -> {
                    Object dataIdValue = data.get(idCode);
                    idMap.put(idCode, dataIdValue);
                });

                bizDataDAO.update(MdUtil.getBizDbCode(taskJob.getTenantId(), taskJob.getProjectId(), taskJob.getEnvId()), dataCode, idMap, data);
            });
            savedDataList.addAll(dataUpdateList);
        }

        taskJob.setProduceDataList(savedDataList);
        taskJob.appendLog("保存业务数据，新增：{} 更新：{}", dataInsertList.size(), dataUpdateList.size());
        taskJob.setInsertCount(taskJob.getInsertCount() + dataInsertList.size());
        taskJob.setUpdateCount(taskJob.getUpdateCount() + dataUpdateList.size());
    }

    /**
     * 保存业务数据的历史记录
     *
     * @param taskJob 任务job
     */
    public void saveProduceDataHistory(TaskJob taskJob) {
        if (taskJob.getEnableHistory() != MdConstant.ENABLED) {
            return;
        }

        List<Map<String, Object>> produceDataList = taskJob.getProduceDataList();
        if (CollUtil.isEmpty(produceDataList)) {
            return;
        }

        produceDataList.forEach(data -> {
            data.remove(MdConstant.MONGODB_OBJECT_ID);
        });

        bizDataDAO.insertBatch(MdUtil.getBizDbCode(taskJob.getTenantId(), taskJob.getProjectId(), taskJob.getEnvId()), taskJob.getDataCode() + MdConstant.DATA_COLUMN_HISTORY, produceDataList);
    }

    /**
     * 导出消费数据的excel文件
     *
     * @param taskJob 任务
     * @return excel文件
     */
    public File exportConsumeDataExcel(TaskJob taskJob) {
        List<Map<String, Object>> consumeDataList = taskJob.getConsumeDataList();
        return exportExcel(consumeDataList, taskJob.getFieldMapping());
    }

    /**
     * 导出过滤数据的excel文件
     *
     * @param taskJob 任务
     * @return excel文件
     */
    public File exportFilteredDataExcel(TaskJob taskJob) {
        List<Map<String, Object>> consumeDataList = taskJob.getFilteredDataList();
        return exportExcel(consumeDataList, taskJob.getFieldMapping());
    }

    /**
     * 将指定数据按任务的字段映射 导出excel文件
     *
     * @param datas         要导出的数据
     * @param mFieldMapping 任务的数据映射，key为字段编号，value为字段名称
     * @return excel文件
     */
    private File exportExcel(List<Map<String, Object>> datas, LinkedHashMap<String, String> mFieldMapping) {
        Assert.notEmpty(mFieldMapping, "任务未选择导出字段");

        // 遍历业务数据，根据映射 转换为excel导出的数据
        List<Map<String, String>> excelDataList = CollUtil.newArrayList();
        datas.forEach(data -> {
            Map<String, String> row = MapUtil.newHashMap();
            mFieldMapping.forEach((k, v) -> {
                row.put(k, ObjectUtil.defaultIfNull(StrUtil.toStringOrNull(data.get(k)), ""));
            });
            excelDataList.add(row);
        });

        // 字段+数据 构建excel
        File excelFile = FileUtil.createTempFile(MdConstant.TEMP_DIR, ".xls", true);
        ExcelWriter excelWriter = ExcelUtil.getWriter();
        // 使用字段名称生成Excel首行标题
        mFieldMapping.forEach(excelWriter::addHeaderAlias);
        // 写入Excel数据
        excelWriter.write(excelDataList);
        excelWriter.autoSizeColumnAll();
        int columnCount = excelWriter.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            excelWriter.setColumnWidth(i, (int) Math.round(SheetUtil.getColumnWidth(excelWriter.getSheet(), i, false)) + 5);
        }
        excelWriter.flush(excelFile);
        excelWriter.close();

        return excelFile;
    }
}
