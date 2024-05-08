package org.springblade.modules.mydata.manage.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用接口调试传输对象实体类
 *
 * @author LIEN
 * @since 2023-03-07
 */
@Data
@EqualsAndHashCode
public class ApiDebugDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求方法
     */
    private String httpMethod;

    /**
     * 请求地址
     */
    private String httpUri;

    /**
     * 请求头
     */
    private List<Map<String, String>> httpHeaders;

    /**
     * 请求参数
     */
    private List<Map<String, Object>> httpParams;

    /**
     * 请求体内容类型
     */
    private String contentType;

    /**
     * 全局header参数
     */
    private LinkedHashMap<String, String> globalHeaders;

    /**
     * 全局变量
     */
    private LinkedHashMap<String, Object> globalParams;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long envId;

    /**
     * 请求体
     */
    private String httpBody;
}
