package tech.zhiwei.frostmetal.modules.mydata.schedule.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import tech.zhiwei.frostmetal.modules.mydata.constant.LiteFlowConstant;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.LFNodeBinding;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.SendDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.email.SendEmail;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline.StopPipeline;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline.TriggerPipeline;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.FilterData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseDataToJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseJsonToData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ProcessData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.WriteDataToExcel;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.script.JsScript;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var.ParseJsonToVar;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var.SetPipelineVar;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.QueryDataFromWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.RemoveData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.SaveDataToWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.webhook.GetJsonFromWebhook;

import java.util.Map;

/**
 * 声明LiteFlow组件
 *
 * @author LIEN
 * @since 2025/11/25
 */
@LiteflowComponent
public class LiteFlowComponentConfig {
    private void process(NodeComponent nodeComponent, TaskExecutor taskExecutor) {
        Map<String, Object> params = nodeComponent.getRequestData();
        Long historyId = (Long) params.get(LiteFlowConstant.BIND_KEY_HISTORY_ID);

        LFNodeBinding nodeBinding = nodeComponent.getBindData(LiteFlowConstant.BIND_KEY_NODE_BINDING, LFNodeBinding.class);

        Map<String, Object> jobContextData = nodeComponent.getFirstContextBean();
        taskExecutor.execute(historyId, nodeBinding.getTaskId(), nodeBinding.getTaskLogId(), jobContextData);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_API_GET_JSON
            , nodeName = "从API获取数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processApiGetJson(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new GetJsonFromApi();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_API_SEND_DATA
            , nodeName = "向API发送数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processApiSendData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new SendDataToApi();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_WEBHOOK_GET_JSON
            , nodeName = "解析webhook数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processWebhookGetJson(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new GetJsonFromWebhook();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_TRIGGER_PIPELINE
            , nodeName = "用Webhook触发流水线"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processTriggerPipeline(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new TriggerPipeline();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_STOP_PIPELINE
            , nodeName = "停止流水线"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processStopPipeline(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new StopPipeline();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_SAVE_DATA
            , nodeName = "保存数据到数仓"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processSaveData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new SaveDataToWarehouse();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_QUERY_DATA
            , nodeName = "从数仓查询数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processQueryData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new QueryDataFromWarehouse();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_REMOVE_DATA
            , nodeName = "从数仓清空指定数据集合"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processRemoveData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new RemoveData();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_JSON_TO_DATA
            , nodeName = "JSON转数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processJsonToData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new ParseJsonToData();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_DATA_TO_JSON
            , nodeName = "数据转JSON"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processDataToJson(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new ParseDataToJson();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_FILTER_DATA
            , nodeName = "过滤数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processFilterData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new FilterData();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_PROCESS_DATA
            , nodeName = "处理数据"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processProcessData(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new ProcessData();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_WRITE_EXCEL
            , nodeName = "数据写入Excel"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processWriteExcel(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new WriteDataToExcel();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_SEND_EMAIL
            , nodeName = "发送邮件"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processSendEmail(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new SendEmail();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_JSON_TO_VAR
            , nodeName = "JSON值存入变量"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processJsonToVar(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new ParseJsonToVar();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_SET_PIPELINE_VAR
            , nodeName = "设置流水线变量"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processSetPipelineVar(NodeComponent nodeComponent) {
        TaskExecutor taskExecutor = new SetPipelineVar();
        process(nodeComponent, taskExecutor);
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS
            , nodeId = MyDataConstant.TASK_TYPE_SCRIPT_JS
            , nodeName = "JS脚本"
            , nodeType = NodeTypeEnum.COMMON
    )
    public void processJsScript(NodeComponent nodeComponent) {
        JsScript jsScript = new JsScript(nodeComponent);
        process(nodeComponent, jsScript);
    }
}
