package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.script;

import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.ScriptTypeEnum;
import com.yomahub.liteflow.script.ScriptExecuteWrap;
import com.yomahub.liteflow.script.ScriptExecutor;
import com.yomahub.liteflow.script.ScriptExecutorFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * js脚本执行器
 */
public class JsScript extends TaskExecutor {

    private final NodeComponent nodeComponent;

    public JsScript(NodeComponent nodeComponent) {
        this.nodeComponent = nodeComponent;
    }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        String jsScript = (String) getTaskConfig().get("SCRIPT");

        if (StringUtil.isEmpty(jsScript)) {
            fail("执行失败：脚本为空");
            return;
        }

        // 记录脚本内容（截断处理）
        String shortScript = StringUtil.sub(jsScript, 0, 500);
        if (jsScript.length() > 500) {
            shortScript += "...(共" + jsScript.length() + "字符)";
        }
        info("脚本内容：\n{}", shortScript);
        info("脚本长度：{} 字符", jsScript.length());

        long startTime = System.currentTimeMillis();

        try {
            // 执行js脚本
            // executeJavaScript(jsScript, pipelineContext);
            executeJavaScriptSafely(jsScript, pipelineContext);
            info("js脚本执行成功");
        } catch (Exception e) {
            fail("js脚本执行异常：{}", e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            info("执行耗时：{} ms", duration);
        }
    }

    /**
     * 执行js脚本 - 使用传入的NodeComponent
     */
    private void executeJavaScript(String script, Map<String, Object> contextData) {
        try {
            info("开始执行js脚本");

            // 检查NodeComponent是否设置
            if (nodeComponent == null) {
                throw new RuntimeException("NodeComponent未设置，请先设置！");
            }

            // 1. 获取ScriptExecutor
            ScriptExecutor scriptExecutor = ScriptExecutorFactory.loadInstance().getScriptExecutor(ScriptTypeEnum.JS.getDisplayName());
            info("创建脚本执行器");

            // 2. 生成节点ID（使用NodeComponent的nodeId）
            String nodeId = nodeComponent.getNodeId() + "_" + System.currentTimeMillis();
            info("生成的节点ID: {}", nodeId);

            // 3. TODO 创建增强脚本
            String enhancedScript = script;

            // 4. 加载脚本到执行器
            scriptExecutor.load(nodeId, script);
            info("加载脚本到执行器");

            // 5. 创建ScriptExecuteWrap（使用传入的NodeComponent）
            ScriptExecuteWrap wrap = createScriptExecuteWrap(nodeId, enhancedScript, contextData);

            // 6. 执行脚本
            info("开始执行脚本...");
            scriptExecutor.executeScript(wrap);
            info("脚本执行完成");
        } catch (Exception e) {
            fail("执行js脚本异常：{}", e.getMessage());
        }
    }

    /**
     * 使用GraalVM独立执行，完全避免LiteFlow依赖
     */
    private void executeJavaScriptSafely(String script, PipelineContext pipelineContext) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = null;

        try {
            future = executor.submit(() -> {
                try {
                    // 使用GraalVM执行
                    HostAccess hostAccess = HostAccess.newBuilder()
                            .allowAccess(PipelineContext.class.getMethod("get", String.class))
                            .allowAccess(PipelineContext.class.getMethod("put", String.class, Object.class))
                            .build();
 
                    Context context = Context.newBuilder("js")
                            .allowHostAccess(hostAccess)
                            .allowIO(false)          // 禁止文件IO
                            .allowCreateThread(false) // 禁止创建线程
                            .allowNativeAccess(false) // 禁止本地代码
                            .allowCreateProcess(false) // 禁止创建进程
                            .allowHostClassLoading(false)  // 禁止动态加载类
                            .build();

                    // 绑定变量
                    Value bindings = context.getBindings("js");
                    bindings.putMember("context", pipelineContext);

                    // 执行脚本
                    context.eval("js", script);
                    context.close();

                } catch (Exception e) {
                    throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
                }
            });

            // 5秒超时
            future.get(5, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            if (future != null) {
                future.cancel(true);
            }
            throw new RuntimeException("脚本执行超时（5秒）");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            throw new RuntimeException(cause != null ? cause.getMessage() : e.getMessage());
        } finally {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
            executor.shutdownNow();
        }
    }

    /**
     * 创建ScriptExecuteWrap - 使用传入的NodeComponent
     */
    private ScriptExecuteWrap createScriptExecuteWrap(String nodeId, String script, Map<String, Object> contextData) {
        ScriptExecuteWrap wrap = new ScriptExecuteWrap();
        // 使用传入的NodeComponent的信息
        wrap.setSlotIndex(nodeComponent.getSlotIndex());
        wrap.setCurrChainId(nodeComponent.getCurrChainId());
        wrap.setNodeId(nodeId);
        wrap.setTag(nodeComponent.getTag());
        wrap.setCmp(nodeComponent);  // 关键：使用传入的NodeComponent
        wrap.setCmpData(contextData);
        return wrap;
    }
}