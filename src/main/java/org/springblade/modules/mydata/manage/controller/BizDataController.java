package org.springblade.modules.mydata.manage.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.SheetUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.dto.UploadBizDataDTO;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.vo.DataFieldVO;
import org.springblade.modules.mydata.manage.vo.UploadExcelVO;
import org.springblade.modules.mydata.manage.wrapper.DataFieldWrapper;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@Slf4j
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

    @PostMapping("/upload_excel")
    public R<UploadExcelVO> uploadExcel(@RequestParam("file") MultipartFile uploadFile) {
        try {
            String path = SystemUtil.getUserInfo().getCurrentDir();
            File dir = new File(path + File.separator + "upload" + File.separator + "excel");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = uploadFile.getOriginalFilename();
            String ext = FileNameUtil.extName(fileName);
            File excelFile = new File(dir, UUID.randomUUID() + "." + ext);

            // 保存excel文件
            uploadFile.transferTo(excelFile);

            // 读取excel 第一行
            ExcelReader excelReader = ExcelUtil.getReader(excelFile);
            List<Object> headerNames = excelReader.readRow(0);
            excelReader.close();

            UploadExcelVO uploadExcelVO = new UploadExcelVO();
            uploadExcelVO.setFileName(excelFile.getName());
            uploadExcelVO.setHeaderNames(headerNames);
            return R.data(uploadExcelVO);
        } catch (IOException e) {
            log.error("上传业务数据excle失败", e);
            return R.fail("上传失败");
        }
    }

    @SneakyThrows
    @GetMapping("/export_excel")
    public ResponseEntity<Resource> exportExcel(@RequestParam Map<String, Object> params) {
        BizDataDTO bizDataDTO = new BizDataDTO();
        bizDataDTO.setProjectId(Long.parseLong(params.remove("projectId").toString()));
        bizDataDTO.setEnvId(Long.parseLong(params.remove("envId").toString()));
        bizDataDTO.setDataId(Long.parseLong(params.remove("dataId").toString()));

        params.remove("current");
        params.remove("size");
        params.remove("blade-auth");

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

        Resource resource = new FileUrlResource(excelFile.getAbsolutePath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFile.getName())
                .body(resource);
    }

    @PostMapping("/save_biz_data")
    public R<Object> saveExcelData(@RequestBody UploadBizDataDTO uploadBizDataDTO) {
        try {
            String fileName = uploadBizDataDTO.getFileName();
            Map<String, String> fieldMapping = uploadBizDataDTO.getFieldMapping();

            String path = SystemUtil.getUserInfo().getCurrentDir();
            File dir = new File(path + File.separator + "upload" + File.separator + "excel");
            File excelFile = new File(dir, fileName);
            if (!excelFile.exists()) {
                return R.fail("上传失败，文件不存在！");
            }

            if (MapUtil.isEmpty(fieldMapping)) {
                return R.fail("上传失败，匹配字段为空，请配置！");
            }

            ExcelReader excelReader = ExcelUtil.getReader(excelFile);

            // 读取excel 第一行
            List<Object> headerNames = excelReader.readRow(0);
            if (CollUtil.isEmpty(headerNames)) {
                return R.fail("上传失败，Excel为空！");
            }

            // 数据字段 与 excel列的映射
            Map<String, Integer> fieldColumnIndexMapping = MapUtil.newHashMap();
            fieldMapping.forEach((dataField, headerName) -> {
                int index = headerNames.indexOf(headerName);
                if (index >= 0) {
                    fieldColumnIndexMapping.put(dataField, index);
                }
            });

            // 从excel的第2行开始读取数据
            List<List<Object>> excelDataList = excelReader.read(1);
            excelReader.close();
            if (CollUtil.isEmpty(excelDataList)) {
                return R.fail("上传失败，Excel为空！");
            }

            List<Map> bizDataList = CollUtil.newArrayList();
            excelDataList.forEach(row -> {
                Map bizData = new HashMap();
                fieldColumnIndexMapping.forEach((dataField, columnIndex) -> {
                    bizData.put(dataField, row.get(columnIndex));
                });
                bizDataList.add(bizData);
            });

            bizDataService.saveBizData(uploadBizDataDTO.getProjectId(), uploadBizDataDTO.getEnvId(), uploadBizDataDTO.getDataId(), bizDataList);
            return R.success("上传成功！");
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.fail("上传失败，请重试！");
        }
    }
}
