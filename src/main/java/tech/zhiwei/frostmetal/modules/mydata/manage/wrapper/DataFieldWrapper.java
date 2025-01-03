package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataFieldVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 标准数据字段 Wrapper
 *
 * @author LIEN
 * @since 2024/11/09
 */
public class DataFieldWrapper extends BaseWrapper<DataField, DataFieldVO> {
    public DataFieldWrapper() {
    }

    public static DataFieldWrapper getInstance() {
        return new DataFieldWrapper();
    }

    @Override
    public DataFieldVO entityVO(DataField entity) {
        return BeanUtil.copyProperties(entity, DataFieldVO.class);
    }
}
