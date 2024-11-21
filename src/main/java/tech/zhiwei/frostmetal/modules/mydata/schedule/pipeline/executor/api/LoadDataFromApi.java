package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.zhiwei.frostmetal.modules.mydata.cache.MdCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * 从API读取数据
 *
 * @author LIEN
 * @since 2024/11/21
 */
public class LoadDataFromApi extends TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(LoadDataFromApi.class);

    public LoadDataFromApi(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public void execute() {
        log.info("从API获取数据 开始");

        PipelineTask pipelineTask = getPipelineTask();
        // 获取应用信息
        App app = MdCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        // 获取接口信息
        AppApi api = MdCache.getApi(pipelineTask.getApiId());
        String apiUrl = api.getApiUri();
        if (StringUtil.isNotEmpty(apiPrefix)) {
            apiUrl = apiPrefix + apiUrl;
        }

        // TODO 分批模式

        // 调用api
        String json = HttpUtil.send(api.getApiMethod(), apiUrl, null, null, null, null);
        log.info("API获取结果：{}", json);

        // 字段映射
        List<Map<String, Object>> fieldMappings = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("fieldMappings");

    }
}
