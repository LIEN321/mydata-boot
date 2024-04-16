package org.springblade.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 集成任务日志视图实体类
 *
 * @author LIEN
 * @since 2022-07-18
 */
@Data
public class TaskLogVO {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属任务
     */
    private Long taskId;

    /**
     * 执行开始时间
     */
    private Date taskStartTime;

    /**
     * 执行结束时间
     */
    private Date taskEndTime;

    /**
     * 执行结果（0-失败，1-成功）
     */
    @JsonSerialize(nullsUsing = NullSerializer.class)
    private Integer taskResult;

    /**
     * 执行内容
     */
    private String taskDetail;
}
