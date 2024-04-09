package org.springblade.modules.mydata.manage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.vo.DataFieldVO;
import org.springblade.modules.mydata.manage.wrapper.DataFieldWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public R<IPage<Map>> list(BizDataDTO bizDataDTO, Query query) {
        return R.data(bizDataService.bizDataPage(Condition.getPage(query), bizDataDTO));
    }

    @GetMapping("/delete_by_env")
    public R deleteByEnv(BizDataDTO bizDataDTO) {
        return R.status(bizDataService.deleteByEnv(bizDataDTO.getDataId(), bizDataDTO.getEnvId()));
    }
}
