package tech.zhiwei.frostmetal.core.base.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.zhiwei.tool.lang.ObjectUtil;

/**
 * 分页查询的参数
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页查询的参数")
public class PageParam {
    /**
     * 分页默认初始业
     */
    private static final long PAGE_DEFAULT_CURRENT = 1;
    /**
     * 分页默认每页数量
     */
    private static final int PAGE_DEFAULT_PAGE_SIZE = 10;
    /**
     * 分页默认总数
     */
    private static final long PAGE_DEFAULT_TOTAL = 0;

    /**
     * 当前页数
     */
    @Schema(description = "当前页数", type = "int")
    private Long current;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", type = "int")
    private Integer pageSize;

    /**
     * 记录总数
     */
    @Schema(description = "记录总数", type = "int")
    private Long total;

    public Long getCurrent() {
        return ObjectUtil.defaultIfNull(this.current, PAGE_DEFAULT_CURRENT);
    }

    public Integer getPageSize() {
        return ObjectUtil.defaultIfNull(this.pageSize, PAGE_DEFAULT_PAGE_SIZE);
    }

    public Long getTotal() {
        return ObjectUtil.defaultIfNull(this.total, PAGE_DEFAULT_TOTAL);
    }
}
