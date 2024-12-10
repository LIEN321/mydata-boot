package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;

import java.util.List;
import java.util.Map;

/**
 * 业务数据，包含 编号、名称、类型、值等信息
 *
 * @author LIEN
 * @since 2024/12/10
 */
@Data
@AllArgsConstructor
public class BizData {
    /**
     * 配置关联的字段列表
     */
    private List<DataField> dataFields;

    /**
     * 业务数据列表
     */
    private List<Map<String, Object>> bizData;
}
