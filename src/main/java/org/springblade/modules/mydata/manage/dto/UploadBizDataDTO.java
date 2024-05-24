package org.springblade.modules.mydata.manage.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 业务数据 DTO
 *
 * @author LIEN
 * @since 2022/7/22
 */
@Data
public class UploadBizDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;

    private Long envId;

    private Long dataId;

    private String fileName;

    private Map<String, String> fieldMapping;
}
