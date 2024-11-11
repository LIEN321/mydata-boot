package tech.zhiwei.frostmetal.core.base.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


/**
 * 项目 VO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "项目 下拉选项VO")
public class SelectVO extends BaseVO {
	@Serial
	private static final long serialVersionUID = 1934671613029991165L;
	@Schema(description = "项目名称")
	private String label;

	@Schema(description = "项目id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long value;
}
