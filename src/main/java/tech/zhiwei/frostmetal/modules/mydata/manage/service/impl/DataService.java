package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.DataDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.DataMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;

import java.util.List;

/**
 * 标准数据 Service实现类
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Service
@AllArgsConstructor
public class DataService extends BaseService<DataMapper, Data> implements IDataService {

    private IDataFieldService dataFieldService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveData(DataDTO dataDTO) {
        check(dataDTO);

        Data data = BeanUtil.copyProperties(dataDTO, Data.class);
        // 保存标准数据
        saveOrUpdate(data);
        // 保存字段列表
        dataFieldService.saveByStandardData(data.getId(), dataDTO.getDataFields());

        return data.getId();
    }

    @Override
    public List<Data> listByProject(Long projectId) {
        LambdaQueryWrapper<Data> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Data::getProjectId, projectId);
        return list(queryWrapper);
    }

    private void check(DataDTO dataDTO) {
        // 参数校验
        Long id = dataDTO.getId();
        String dataCode = dataDTO.getDataCode();
        String dataName = dataDTO.getDataName();
        Long projectId = dataDTO.getProjectId();

        // 新增数据项 校验编号，更新操作 不支持修改编号
        if (id == null) {
            // 数据项编号 不能为空
            AssertUtil.notBlank(dataCode, "提交失败：编号 不能为空！");
            // 数据项编号 长度不能超过限制
            AssertUtil.isTrue(dataCode.length() <= MdConstant.MAX_CODE_LENGTH, "提交失败：编号 不能超过{}位！", MdConstant.MAX_CODE_LENGTH);

            // 校验code是否唯一
            Data check = findByCode(projectId, dataCode);
            AssertUtil.isNull(check, "提交失败：编号 {} 已存在！", dataCode);
        }

        // 数据项名称 不能为空
        AssertUtil.notBlank(dataName, "提交失败：名称 不能为空！");
        // 数据项名称 长度不能超过限制
        AssertUtil.isTrue(dataName.length() <= MdConstant.MAX_NAME_LENGTH, "提交失败：名称 不能超过{}位！", MdConstant.MAX_NAME_LENGTH);
    }

    /**
     * 根据编号查询 唯一的数据项
     *
     * @param code 编号
     * @return 数据项
     */
    private Data findByCode(Long projectId, String code) {
        LambdaQueryWrapper<Data> queryWrapper = Wrappers.<Data>lambdaQuery()
                .eq(Data::getProjectId, projectId)
                .eq(Data::getDataCode, code);
        return getOne(queryWrapper);
    }
}
