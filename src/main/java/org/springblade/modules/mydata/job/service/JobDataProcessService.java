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
     * 根据字段映射配置 处理业务数据
     *
     * @param taskInfo    任务
     * @param pendingData 待处理数据
     * @param originData  原始的业务数据
     */
    public void processBizData(TaskInfo taskInfo, Map<String, Object> pendingData, Map<String, Object> originData) {
        // 任务中的字段数据处理配置
        Map<String, Map<String, String>> dataProcess = taskInfo.getDataProcess();

        // 数据处理，{fieldCode:{op:op,v:value}, ...}
        if (MapUtil.isEmpty(dataProcess)) {
            return;
        }

        Set<String> fieldCodes = dataProcess.keySet();
        for (String fieldCode : fieldCodes) {
            // {op:op,v:value}
            Map<String, String> processOp = dataProcess.get(fieldCode);
            // 处理前的字段值
            Object originValue = pendingData.get(fieldCode);
            // 处理操作
            String op = processOp.get(MdConstant.PARAM_OP);

            // 优先处理置空
            if (isSetNull(op)) {
                pendingData.put(fieldCode, null);
                continue;
            }

            if (ObjectUtil.isNull(originValue) || MapUtil.isEmpty(processOp)) {
                continue;
            }
            // 处理值
            String opValue = processOp.get(MdConstant.PARAM_VALUE);
            if (StrUtil.isNotEmpty(op)) {
                if (MapUtil.isEmpty(originData)) {
                    continue;
                }
                // 解析处理值中的表达式 {fieldCode}
                opValue = JobVarService.parseDataVar(opValue, originData);
                try {
                    // 获取新的值
                    Object newValue = processValue(originValue, op, opValue, originData);
                    // 存入业务数据
                    pendingData.put(fieldCode, newValue);
                } catch (Exception e) {
                    ExceptionUtil.wrapRuntimeAndThrow(StrUtil.format("处理字段值出错，字段名={} 字段值={} 操作={} 操作值={} 业务数据={}，错误：{}", fieldCode, originValue, op, opValue, originData, e.getMessage()));
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
            // 字符串：md5，base64
            case "md5":
                return MD5.create().digestHex(StrUtil.toString(originValue));
            case "base64":
                return Base64.encode(StrUtil.toString(originValue));
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
