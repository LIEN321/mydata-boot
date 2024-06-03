package org.springblade.modules.mydata.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * 任务的数据 排序条件
 *
 * @author LIEN
 * @since 2024/6/3
 */
@Data
@AllArgsConstructor
public class BizDataSort {
    // 排序字段名
    private String name;

    // 排序方向
    private Sort.Direction direction;
}