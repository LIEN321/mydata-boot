package org.springblade.modules.mydata.job.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springblade.common.util.HttpUtils;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Api接口调用器
 *
 * @author LIEN
 * @since 2021/1/31
 */
@Component
public class ApiUtil {

    /**
     * 调用接口，获取返回结果
     *
     * @param taskInfo 任务
     * @return 接口结果
     */
    public static String read(TaskInfo taskInfo) {
        return HttpUtils.send(Method.valueOf(taskInfo.getApiMethod()), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams(), taskInfo.getReqBody());
    }

    /**
     * 调用接口，发送标准数据
     *
     * @param taskInfo 任务
     */
    public static String write(TaskInfo taskInfo) {
        return write(taskInfo, taskInfo.getConsumeDataList());
    }

    public static String write(TaskInfo taskInfo, List<Map> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return "";
        }

        String apiFieldPrefix = taskInfo.getApiFieldPrefix();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(dataList);
        JSON json = jsonArray;

        if (StrUtil.isNotBlank(apiFieldPrefix)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putByPath(apiFieldPrefix, json);
            json = jsonObject;
        }

        return HttpUtils.send(Method.valueOf(taskInfo.getApiMethod()), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams(), json.toString());
    }

    public static void write(TaskInfo taskInfo, Map data) {
        if (CollUtil.isEmpty(data)) {
            return;
        }

        JSON json;
        String body = taskInfo.getReqBody();
        // 有body时，直接发送body内容
        if (StrUtil.isNotEmpty(body)) {
            json = new JSONObject(body);
        } else {
            json = new JSONObject(data);

            String apiFieldPrefix = taskInfo.getApiFieldPrefix();
            if (StrUtil.isNotBlank(apiFieldPrefix)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.putByPath(apiFieldPrefix, json);
                json = jsonObject;
            }
        }

        HttpUtils.send(Method.valueOf(taskInfo.getApiMethod()), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams(), json.toString());
    }
}