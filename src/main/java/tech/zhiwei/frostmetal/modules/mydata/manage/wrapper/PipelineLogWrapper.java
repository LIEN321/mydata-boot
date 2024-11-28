package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineLogVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线执行日志 Wrapper
 *
 * @author LIEN
 * @since 2024/11/28
 */
public class PipelineLogWrapper extends BaseWrapper<PipelineLog, PipelineLogVO> {
    public PipelineLogWrapper() {
    }

    public static PipelineLogWrapper getInstance() {
        return new PipelineLogWrapper();
    }

    @Override
    public PipelineLogVO entityVO(PipelineLog entity) {
        return BeanUtil.copyProperties(entity, PipelineLogVO.class);
    }
}
