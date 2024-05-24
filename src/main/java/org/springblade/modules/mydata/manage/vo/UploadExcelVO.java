package org.springblade.modules.mydata.manage.vo;

import lombok.Data;

import java.util.List;

/**
 * 上传Excel文件VO
 *
 * @author LIEN
 * @since 2024/5/24
 */
@Data
public class UploadExcelVO {
    /**
     * Excel文件名
     */
    private String fileName;

    /**
     * Excel表头名
     */
    private List<Object> headerNames;
}
