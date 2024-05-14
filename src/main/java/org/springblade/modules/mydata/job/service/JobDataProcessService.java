package org.springblade.modules.mydata.job.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.expression.ExpressionUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 业务数据处理
 *
 * @author LIEN
 * @since 2024/5/3
 */
@Component
public class JobDataProcessService {
    /**
     * 根据字段映射配置处理 提供 的业务数据
     *
     * @param taskInfo      任务
     * @param processedData 待处理数据
     * @param originData    原始的业务数据
     */
    public void processProduceData(TaskInfo taskInfo, Map<String, Object> processedData, Map<String, Object> originData) {
        processBizData(taskInfo, processedData, originData, false);
    }

    /**
     * 根据字段映射配置处理 消费 的业务数据
     *
     * @param taskInfo      任务
     * @param processedData 待处理数据
     */
    public void processConsumeData(TaskInfo taskInfo, Map<String, Object> processedData) {
        processBizData(taskInfo, processedData, null, true);
    }

    /**
     * 根据字段映射配置 处理业务数据
     *
     * @param taskInfo      任务
     * @param processedData 待处理数据
     * @param originData    原始的业务数据
     * @param forceToEmpty  是否将null值转为empty
     */
    public void processBizData(TaskInfo taskInfo, Map<String, Object> processedData, Map<String, Object> originData, boolean forceToEmpty) {
        // 任务中的字段数据处理配置
        Map<String, Map<String, String>> dataProcess = taskInfo.getDataProcess();

        // 数据处理，{fieldCode:{op:op,v:value}, ...}
        if (MapUtil.isEmpty(dataProcess)) {
            return;
        }

        Set<String> fieldCodes = dataProcess.keySet();
        for (String fieldCode : fieldCodes) {
            // 获取字段的操作配置 {op:op,v:value}
            Map<String, String> processMap = dataProcess.get(fieldCode);
            // 若操作配置无效，则跳过
            if (MapUtil.isEmpty(processMap)) {
                continue;
            }

            // 操作类型
            String op = processMap.get(MdConstant.PARAM_OP);

            // 优先处理 置空 操作
            if (isSetNull(op)) {
                processedData.put(fieldCode, forceToEmpty ? StrUtil.EMPTY : null);
                continue;
            }

            // 处理前的字段值
            Object originValue = processedData.get(fieldCode);
            // 若字段值无效
            if (ObjectUtil.isNull(originValue)) {
                // 若置empty
                if (forceToEmpty) {
                    processedData.put(fieldCode, StrUtil.EMPTY);
                }
                // 否则不处理
                continue;
            }
            // 处理值
            String opValue = processMap.get(MdConstant.PARAM_VALUE);
            if (StrUtil.isNotEmpty(op)) {
                if (MapUtil.isEmpty(processedData)) {
                    continue;
                }
                // 先解析 {{$field}}
                opValue = JobVarService.parseExistedDataVar(opValue, originData, taskInfo.getFieldTypeMapping());
                // 再解析 {{field}}
                opValue = JobVarService.parseDataFieldVar(opValue, processedData, taskInfo.getFieldTypeMapping());
                try {
                    // 获取新的值
                    Object newValue = processValue(originValue, op, opValue, processedData);
                    // 存入业务数据
                    processedData.put(fieldCode, newValue);
                } catch (Exception e) {
                    ExceptionUtil.wrapRuntimeAndThrow(StrUtil.format("处理字段值出错，字段名={} 字段值={} 操作={} 操作值={} 业务数据={}，错误：{}", fieldCode, originValue, op, opValue, processedData, e.getMessage()));
                }
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
                if (ObjectUtil.isNull(opValue)) {
                    return originValue;
                }
                return ExpressionUtil.eval(StrUtil.toString(originValue) + op + opValue, originData);
            // 字符串：md5，base64，prepend，append，set empty
            case "md5":
                return MD5.create().digestHex(StrUtil.toString(originValue));
            case "base64":
                return Base64.encode(StrUtil.toString(originValue));
            case "prepend":
                return StrUtil.prependIfMissing(StrUtil.toString(originValue), StrUtil.toString(opValue));
            case "append":
                return StrUtil.appendIfMissing(StrUtil.toString(originValue), StrUtil.toString(opValue));
            case "set empty":
                return StrUtil.EMPTY;
            // 日期：add second
            case "add second":
                Date date = DateUtil.parse(StrUtil.toString(originValue));
                Calendar calendar = CalendarUtil.calendar(date);
                calendar.add(Calendar.SECOND, NumberUtil.parseInt(StrUtil.toString(opValue)));
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
        return "set null".equals(targetOp);
    }
}
