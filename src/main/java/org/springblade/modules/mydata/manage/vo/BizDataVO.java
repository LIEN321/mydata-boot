package org.springblade.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务数据概况 视图类
 *
 * @author LIEN
 * @since 2024/4/26
 */
@Data
public class BizDataVO implements Serializable {
    private static final long serialVersionUID = 7327652152184844274L;
    /**
     * 项目id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 环境id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long envId;

    /**
     * 环境名称
     */
    private String envName;

    /**
     * 数据项id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataId;

    /**
     * 数据量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataCount;
}
