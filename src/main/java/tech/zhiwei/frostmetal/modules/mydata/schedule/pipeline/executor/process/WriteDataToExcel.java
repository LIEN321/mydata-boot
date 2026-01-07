package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process;

import cn.hutool.core.date.DatePattern;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.apache.poi.ss.util.SheetUtil;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.io.FileUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 业务数据写到Excel
 *
 * @author LIEN
 * @since 2024/12/11
 */
public class WriteDataToExcel extends TaskExecutor {
    // public WriteDataToExcel(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        PipelineTask pipelineTask = getPipelineTask();
        // 输入参数
        Map<String, String> inputMap = getInputMap();

        // 获业务数据的key
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
//            error("业务数据的变量名为空，结束执行。");
            throw new IllegalArgumentException("业务数据的变量名为空，结束执行。");
        }

        // 业务数据集合
        PipelineBizData pipelineBizData = (PipelineBizData) pipelineContext.get(bizDataKey);
        if (pipelineBizData == null) {
//            error("执行失败：前置任务没有输出有效的业务数据");
            throw new IllegalArgumentException("执行失败：前置任务没有输出有效的业务数据");
        }
        List<Map<String, Object>> bizDataList = pipelineBizData.getBizData();
        if (CollectionUtil.isEmpty(bizDataList)) {
            info("没有待保存的业务数据，结束执行。");
            return;
        }

        // 所属项目信息
        Project project = MyDataCache.getProject(pipelineTask.getProjectId());

        // 标准数据字段列表
        List<DataField> dataFields = pipelineBizData.getDataFields();
        List<DataField> visibleDataFields = dataFields.stream().filter(DataField::getDisplayMode).toList();
        info("开始导出Excel文件");
        File tempExcel = exportTempExcel(visibleDataFields, bizDataList);
        tempExcel = FileUtil.rename(tempExcel, pipelineTask.getTaskName() + "_" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN), true, true);
        File savedExcelFile = MyDataUtil.saveExcelFile(pipelineTask.getTenantId(), project.getProjectCode(), tempExcel);
        info("导出文件成功，文件名：{}", tempExcel.getName());

        pipelineContext.put(MyDataConstant.JOB_DATA_KEY_EXCEL_FILE, savedExcelFile);
    }

    private File exportTempExcel(List<DataField> dataFields, List<Map<String, Object>> bizDataList) {
        // 字段code : 字段name
        Map<String, String> filedMapping = MapUtil.newHashMap(true);
        dataFields.forEach(field -> {
            filedMapping.put(field.getFieldCode(), field.getFieldName());
        });

        // 遍历业务数据，根据映射 转换为excel导出的数据
        List<Map<String, String>> excelDataList = CollectionUtil.newArrayList();
        bizDataList.forEach(bizData -> {
            Map<String, String> row = MapUtil.newHashMap();
            filedMapping.forEach((code, name) -> {
                // 字段名:数据值
                row.put(code, StringUtil.toStringOrEmpty(bizData.get(code)));
            });
            excelDataList.add(row);
        });

        // 字段+数据 构建excel
        File excelFile = FileUtil.createTempFile(MyDataConstant.TEMP_DIR, ".xls", true);
        ExcelWriter excelWriter = ExcelUtil.getWriter();
        // 使用字段名称生成Excel首行标题
        filedMapping.forEach(excelWriter::addHeaderAlias);
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
