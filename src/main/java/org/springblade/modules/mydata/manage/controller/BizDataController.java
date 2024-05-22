package org.springblade.modules.mydata.manage.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.util.SheetUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.vo.DataFieldVO;
import org.springblade.modules.mydata.manage.wrapper.DataFieldWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * 业务数据 控制器
 *
 * @author LIEN
 * @since 2022/7/22
 */
@RestController
@AllArgsConstructor
@RequestMapping(MdConstant.API_PREFIX_MANAGE + "/biz_data")
@Api(value = "标准数据项", tags = "标准数据项接口")
public class BizDataController {

    private final IDataFieldService dataFieldService;

    private final IBizDataService bizDataService;

    /**
     * 根据数据项 查询字段列表
     *
     * @param dataId 数据项id
     * @return 字段列表
     */
    @GetMapping("/field_list")
    public R<List<DataFieldVO>> listDataFields(Long dataId) {
        return R.data(DataFieldWrapper.build().listVO(dataFieldService.findByData(dataId)));
    }

    @GetMapping("/data_list")
    public R<IPage<Map>> list(@RequestParam Map<String, Object> params) {
        BizDataDTO bizDataDTO = new BizDataDTO();
        bizDataDTO.setProjectId(Long.parseLong(params.remove("projectId").toString()));
        bizDataDTO.setEnvId(Long.parseLong(params.remove("envId").toString()));
        bizDataDTO.setDataId(Long.parseLong(params.remove("dataId").toString()));

        Query query = new Query();
        query.setSize(Integer.parseInt(params.remove("size").toString()));
        query.setCurrent(Integer.parseInt(params.remove("current").toString()));
        return R.data(bizDataService.bizDataPage(Condition.getPage(query), bizDataDTO, params));
    }

    @GetMapping("/delete_by_env")
    public R deleteByEnv(BizDataDTO bizDataDTO) {
        return R.status(bizDataService.deleteByEnv(bizDataDTO.getDataId(), bizDataDTO.getEnvId()));
    }

    @SneakyThrows
    @GetMapping("/export_excel")
    public void exportExcel(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        BizDataDTO bizDataDTO = new BizDataDTO();
        bizDataDTO.setProjectId(Long.parseLong(params.remove("projectId").toString()));
        bizDataDTO.setEnvId(Long.parseLong(params.remove("envId").toString()));
        bizDataDTO.setDataId(Long.parseLong(params.remove("dataId").toString()));

        Long dataId = bizDataDTO.getDataId();
        Data data = ManageCache.getData(dataId);

        List<DataField> dataFields = dataFieldService.findByData(dataId);
        List<Map> bizDataList = bizDataService.bizDataList(bizDataDTO, params);
        List<Map<String, String>> excelDataList = CollUtil.newArrayList();
        bizDataList.forEach(bizData -> {
            Map<String, String> row = MapUtil.newHashMap();
            dataFields.forEach(dataField -> {
                row.put(dataField.getFieldCode(), ObjectUtil.defaultIfNull(StrUtil.toStringOrNull(bizData.get(dataField.getFieldCode())), ""));
            });
            excelDataList.add(row);
        });

        File excelFile = FileUtil.createTempFile(MdConstant.TEMP_DIR, ".xls", true);
        ExcelWriter excelWriter = ExcelUtil.getWriter();
        // 使用字段名称生成Excel首行标题
        dataFields.forEach(dataField -> {
            excelWriter.addHeaderAlias(dataField.getFieldCode(), dataField.getFieldName());
        });

        // 写入Excel数据
        excelWriter.write(excelDataList);
        excelWriter.autoSizeColumnAll();
        int columnCount = excelWriter.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            excelWriter.setColumnWidth(i, (int) Math.round(SheetUtil.getColumnWidth(excelWriter.getSheet(), i, false)) + 5);
        }
        excelWriter.flush(excelFile);
        excelWriter.close();

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String fileName = URLEncoder.encode(data.getDataName() + " 业务数据导出", StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        IoUtil.copy(Files.newInputStream(excelFile.toPath()), response.getOutputStream());
    }
}
