package tech.zhiwei.frostmetal.core.base.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 请求响应
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "请求响应")
public class P<T> extends R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 423889165770398078L;

    // 响应状态码
    @Schema(description = "当前页数")
    private long current;

    @Schema(description = "每页记录数量")
    private long pageSize;

    @Schema(description = "记录总数")
    private long total;

    private P(int code, T data, String message) {
        super(code, data, message);
    }

    /**
     * 成功返回数据
     *
     * @param data 数据
     * @param <T>  泛型
     * @return 响应结果
     */
    public static <T> P<T> data(T data) {
        return new P<>(ResponseCode.SUCCESS.getCode(), data, ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 返回分页数据
     *
     * @param page 分页查询结果
     * @param <T>  泛型
     * @return 分页结果
     */
    public static <T> P<List<T>> page(IPage<T> page) {
        P<List<T>> p = data(page.getRecords());
        p.setCurrent(page.getCurrent());
        p.setPageSize(page.getSize());
        p.setTotal(page.getTotal());
        return p;
    }

}
