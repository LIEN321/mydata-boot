package tech.zhiwei.frostmetal.dev.generator.bean;

import lombok.Data;
import tech.zhiwei.frostmetal.core.constant.DevConstant;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;

/**
 * 生成代码的实体
 *
 * @author LIEN
 * @since 2024/9/29
 */
@Data
public class Entity {

    public Entity(String entityCode, String entityName, String packageName, String extendMode, String tableName,
                  List<Property> properties) {
        this.entityCode = StringUtil.toCamelCase(entityCode);
        this.entityName = entityName;
        this.packageName = packageName;
        this.extendMode = extendMode;
        this.tableName = tableName;
        this.properties = properties;

        this.entityClassName = StringUtil.upperFirst(this.entityCode);

        switch (extendMode) {
            case DevConstant.EXTEND_MODE_ID -> {
                parentDTOClass = "BaseDTO";
                parentEntityPackage = "base";
                parentEntityClass = "IdEntity";
                parentIServiceClass = "IIdService";
                parentServiceClass = "IdService";
                parentVOClass = "BaseVO";
            }
            case DevConstant.EXTEND_MODE_BASE -> {
                parentDTOClass = "BaseDTO";
                parentEntityPackage = "base";
                parentEntityClass = "BaseEntity";
                parentIServiceClass = "IBaseService";
                parentServiceClass = "BaseService";
                parentVOClass = "BaseVO";
            }
            case DevConstant.EXTEND_MODE_TREE -> {
                parentDTOClass = "TreeDTO";
                parentEntityPackage = "base";
                parentEntityClass = "TreeEntity";
                parentIServiceClass = "ITreeService";
                parentServiceClass = "TreeService";
                parentVOClass = "TreeVO";
            }
            case DevConstant.EXTEND_MODE_TENANT -> {
                parentDTOClass = "BaseDTO";
                parentEntityPackage = "tenant";
                parentEntityClass = "TenantEntity";
                parentIServiceClass = "IBaseService";
                parentServiceClass = "BaseService";
                parentVOClass = "BaseVO";
            }
            case DevConstant.EXTEND_MODE_TREE_TENANT -> {
                parentDTOClass = "TreeDTO";
                parentEntityPackage = "tenant";
                parentEntityClass = "TenantTreeEntity";
                parentIServiceClass = "ITreeService";
                parentServiceClass = "TreeService";
                parentVOClass = "TreeVO";
            }
        }
    }

    /**
     * 实体编号
     */
    private String entityCode;

    /**
     * 实体名称
     */
    private String entityName;

    /**
     * java代码的package
     */
    private String packageName;

    /**
     * 基础实体模式
     */
    private String extendMode;

    private String parentDTOClass;

    private String parentEntityPackage;
    private String parentEntityClass;

    private String parentIServiceClass;

    private String parentServiceClass;

    private String parentVOClass;

    /**
     * 数据库表名
     */
    private String tableName;

    /**
     * 实体类名
     */
    private String entityClassName;

    /**
     * 属性列表
     */
    private List<Property> properties;
}
