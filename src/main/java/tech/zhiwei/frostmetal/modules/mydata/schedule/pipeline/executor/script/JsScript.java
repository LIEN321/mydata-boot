package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.script;

import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Map;

/**
 * JS脚本
 *
 * @author LIEN
 * @since 2026/1/4
 */
public class JsScript extends TaskExecutor {
    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        String jsScript = (String) getTaskConfig().get("SCRIPT");

        if (StringUtil.isEmpty(jsScript)) {
            fail("执行失败：脚本为空");
        }

        info("执行脚本：\n{}", jsScript);
    }
}
