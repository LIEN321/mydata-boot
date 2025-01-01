package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.expression.ExpressionUtil;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.BizDataProcess;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobVarService;
import tech.zhiwei.tool.codec.Base64;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.CalendarUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.NumberUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理数据
 *
 * @author LIEN
 * @since 2024/12/12
 */
public class ProcessData extends TaskExecutor {
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);

    public ProcessData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        // 输入参数
        Map<String, String> inputMap = getInputMap();
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
//            error("执行失败：未配置待处理的业务数据变量，无法获取业务数据");
            throw new IllegalArgumentException("执行失败：未配置待处理的业务数据变量，无法获取业务数据");
        }

        // 获取上下文的业务数据
        PipelineBizData pipelineBizData = (PipelineBizData) jobContextData.get(bizDataKey);
        if (pipelineBizData == null) {
//            error("执行失败：前置任务没有输出有效的业务数据");
            throw new IllegalArgumentException("执行失败：前置任务没有输出有效的业务数据");
        }
        List<Map<String, Object>> bizDataList = pipelineBizData.getBizData();
        if (CollectionUtil.isEmpty(bizDataList)) {
            log("待处理的数据为空，结束执行");
            return;
        }

        // 处理方式
        List<Map<String, Object>> dataProcessConfig = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("DATA_PROCESS");
        List<BizDataProcess> dataProcesses = convertBizDataProcess(dataProcessConfig);
        if (CollectionUtil.isEmpty(dataProcesses)) {
//            error("执行失败：未配置处理方式，结束执行");
            throw new RuntimeException("执行失败：未配置处理方式，结束执行");
        }

        Long dataId = pipelineBizData.getDataId();
        if (ObjectUtil.isNull(dataId)) {
//            error("执行失败：前置任务未选择标准数据，结束执行");
            throw new RuntimeException("执行失败：前置任务未选择标准数据，结束执行");
        }

        // 输出参数
        Map<String, String> outputMap = getOutputMap();
        String filteredDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(filteredDataKey)) {
//            error("执行失败：无效的输出设置，未配置处理结果的变量名");
            throw new RuntimeException("执行失败：无效的输出设置，未配置处理结果的变量名");
        }

        Data data = MyDataCache.getData(dataId);
        // 标准数据字段列表
        List<DataField> dataFields = pipelineBizData.getDataFields();
        // 从字段列表提取标识字段
        List<DataField> idFields = dataFields.stream().filter(DataField::getIsId).toList();

        // 字段编号-字段类型
        Map<String, String> fieldTypeMapping = dataFields.stream().collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));

        log("处理前的数据：{}", bizDataList);
        log("处理数据开始...");

        // 遍历数据，并进行处理
        bizDataList.forEach(bizData -> {
            processBizData(data, bizData, jobContextData, idFields, fieldTypeMapping, dataProcesses);
        });
        log("处理后的数据：{}", bizDataList);
        log("处理数据结束");

        // 输出参数
        jobContextData.put(bizDataKey, pipelineBizData);
    }

    /**
     * 将数据的处理配置 转为BizDataProcess对象
     *
     * @param dataProcessConfig 数据的处理配置
     * @return 数据处理方式集合
     */
    private List<BizDataProcess> convertBizDataProcess(List<Map<String, Object>> dataProcessConfig) {
        if (CollectionUtil.isEmpty(dataProcessConfig)) {
            return null;
        }

        List<BizDataProcess> bizDataProcessList = CollectionUtil.newArrayList();
        for (Map<String, Object> map : dataProcessConfig) {
            BizDataProcess bizDataProcess = new BizDataProcess();
            bizDataProcess.setKey(map.get("k").toString());
            bizDataProcess.setOp(map.get("op").toString());
            bizDataProcess.setValue(map.get("v"));
            bizDataProcess.setType(map.get("t"));

            if (StringUtil.isEmpty(bizDataProcess.getOp())) {
//                error("字段{} 未配置处理方式", bizDataProcess.getKey());
                throw new IllegalArgumentException(StringUtil.format("字段{} 未配置处理方式", bizDataProcess.getKey()));
            }

            bizDataProcessList.add(bizDataProcess);
        }

        return bizDataProcessList;
    }

    /**
     * 根据配置 处理业务数据
     *
     * @param bizData            待处理的业务数据
     * @param fieldTypeMapping   数据字段类型
     * @param bizDataProcessList 处理方式
     */
    private void processBizData(Data data, Map<String, Object> bizData, Map<String, Object> jobContextData, List<DataField> idFields, Map<String, String> fieldTypeMapping, List<BizDataProcess> bizDataProcessList) {
        for (BizDataProcess bizDataProcess : bizDataProcessList) {
            // 处理的字段编号
            String key = bizDataProcess.getKey();
            // 处理
            String op = bizDataProcess.getOp();
            // 处理值
            Object opValue = bizDataProcess.getValue();
            // 处理类型
            Object type = bizDataProcess.getType();

            // 优先处理 置空 操作
            if (isSetNull(op)) {
                bizData.put(key, null);
                continue;
            }
            if (isSetEmpty(op)) {
                bizData.put(key, StringUtil.EMPTY);
                continue;
            }

            // 当数据中 指定字段的值无效，则过滤该数据
            Object dataValue = bizData.get(key);

            // 若字段值无效
            if (ObjectUtil.isNull(dataValue)) {
                // 需要设置为空字符串
                if (isSetEmptyIfNull(op)) {
                    dataValue = StringUtil.EMPTY;
                } else {
                    // 跳过不处理
                    continue;
                }
            }
            try {
                // 标识字段 键值对
                Map<String, Object> idMap = MapUtil.newHashMap();
                for (DataField idField : idFields) {
                    String idCode = idField.getFieldCode();
                    Object idFieldValue = bizData.get(idCode);
                    idMap.put(idCode, idFieldValue);
                }

                // 根据唯一标识 查询原有的业务数据
                Map<String, Object> queryData = bizDataDAO.findByIds(getWarehouseName(), data.getDataCode(), idMap);

                if (MapUtil.isNotEmpty(queryData)) {
                    // 解析 {$field}
                    opValue = JobVarService.processExistedDataVar(opValue.toString(), queryData, fieldTypeMapping);
                }
                // 解析 ${field}
                opValue = JobVarService.processDataFieldVar(opValue.toString(), MapUtil.union(bizData, jobContextData), fieldTypeMapping);

                if (MyDataConstant.TASK_FILTER_TYPE_FIELD.equals(type)) {
                    // 处理值是字段，从数据中取出字段的值
                    opValue = bizData.get(opValue);
                }
                Object newValue = processValue(dataValue, op, opValue, bizData);
                bizData.put(key, newValue);
            } catch (Exception e) {
//                error("处理字段值出错：字段名={}，字段值={}，操作={}，操作值={}，错误：{}", key, dataValue, op, opValue, e.getMessage());
                throw new RuntimeException(StringUtil.format("处理字段值出错：字段名={}，字段值={}，操作={}，操作值={}，错误：{}", key, dataValue, op, opValue, e.getMessage()), e);
            }
        }
    }

    /**
     * 根据op类型 结合opValue和originData  处理originValue
     *
     * @param originValue 处理前的数据值
     * @param op          操作类型
     * @param opValue     操作值
     * @param originData  处理前业务数据
     * @return 处理后的数据
     */
    private Object processValue(Object originValue, String op, Object opValue, Map<String, Object> originData) {
        switch (op) {
            // 数值运算： + - * /
            case "+":
            case "-":
            case "*":
            case "/":
                if (cn.hutool.core.util.ObjectUtil.isNull(opValue)) {
                    return originValue;
                }
                return ExpressionUtil.eval(StringUtil.toString(originValue) + op + opValue, originData);
            // 字符串：md5，base64，prepend，append
            case "md5":
                return MD5.create().digestHex(StringUtil.toString(originValue));
            case "base64":
                return Base64.encode(StringUtil.toString(originValue));
            case "prepend":
                return StringUtil.prependIfMissing(StringUtil.toString(originValue), StringUtil.toString(opValue));
            case "append":
                return StringUtil.appendIfMissing(StringUtil.toString(originValue), StringUtil.toString(opValue));
            // 日期：add second
            case "addSecond":
                Date date = DateUtil.parse(StringUtil.toString(originValue));
                Calendar calendar = CalendarUtil.calendar(date);
                calendar.add(Calendar.SECOND, NumberUtil.parseInt(StringUtil.toString(opValue)));
                return calendar.getTime();
        }
        return originValue;
    }

    /**
     * 判断指定处理类型 是否为置空null
     *
     * @param targetOp 指定处理类型
     * @return true-为置空，false-不是
     */
    public static boolean isSetNull(String targetOp) {
        return "null".equals(targetOp);
    }

    /**
     * 判断指定处理类型 是否为置空empty
     *
     * @param targetOp 指定处理类型
     * @return true-为置空，false-不是
     */
    public static boolean isSetEmpty(String targetOp) {
        return "empty".equals(targetOp);
    }

    /**
     * 判断指定处理类型 是否为置空empty
     *
     * @param targetOp 指定处理类型
     * @return true-为置空，false-不是
     */
    public static boolean isSetEmptyIfNull(String targetOp) {
        return "emptyIfNull".equals(targetOp);
    }
}
