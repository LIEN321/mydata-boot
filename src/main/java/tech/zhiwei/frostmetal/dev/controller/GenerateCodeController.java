package tech.zhiwei.frostmetal.dev.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.dev.dto.GenerateCodeDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntity;
import tech.zhiwei.frostmetal.dev.entity.DevEntityProperty;
import tech.zhiwei.frostmetal.dev.generator.CodeGenerator;
import tech.zhiwei.frostmetal.dev.generator.bean.Entity;
import tech.zhiwei.frostmetal.dev.generator.bean.Property;
import tech.zhiwei.frostmetal.dev.service.IDevEntityPropertyService;
import tech.zhiwei.frostmetal.dev.service.IDevEntityService;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.List;

/**
 * 生成代码 Controller
 *
 * @author LIEN
 * @since 2024/9/29
 */
@RestController
@RequestMapping("/generate_code")
@AllArgsConstructor
@Tag(name = "generate_code", description = "生成代码API")
public class GenerateCodeController {
    private IDevEntityService devEntityService;
    private IDevEntityPropertyService devEntityPropertyService;

    @PostMapping
    public R<Object> generateCode(@RequestBody GenerateCodeDTO generateCodeDTO) {
        Long[] ids = generateCodeDTO.getIds();

        if (ObjectUtil.isEmpty(ids)) {
            return R.fail("生成代码失败：请选择实体");
        }

        List<DevEntity> devEntities = CollectionUtil.newArrayList();

        for (Long id : ids) {
            // 根据id查询DevEntity和DevEntityField
            DevEntity devEntity = devEntityService.getById(id);
            AssertUtil.notNull(devEntity, "业务实体不存在，请确认");

            Long entityId = devEntity.getId();
            List<DevEntityProperty> devEntityProperties = devEntityPropertyService.listByEntityId(entityId);

            // 将 devEntity、devEntityProperties 转为Entity、Property对象
            List<Property> properties = CollectionUtil.newArrayList();
            if (CollectionUtil.isNotEmpty(devEntityProperties)) {
                devEntityProperties.forEach(p -> {
                    Property property = new Property(p.getCode(), p.getName(), p.getType(), p.getIsList(), p.getIsSearch(), p.getSearchType(), p.getIsForm(), p.getFormComponent(), p.getIsRequired());
                    properties.add(property);
                });
            }

            Entity entity = new Entity(devEntity.getCode(), devEntity.getName(), devEntity.getPackageName(), devEntity.getExtendMode(), devEntity.getTableName(), properties);

            // 生成代码
            CodeGenerator.generate(entity, generateCodeDTO.getAuthName(), generateCodeDTO.getJavaCodePath(), generateCodeDTO.getUiCodePath());


            // 更新生成代码的配置
            devEntity.setAuthName(generateCodeDTO.getAuthName());
            devEntity.setJavaCodePath(generateCodeDTO.getJavaCodePath());
            devEntity.setUiCodePath(generateCodeDTO.getUiCodePath());

            // 加入列表，待批量更新
            devEntities.add(devEntity);
        }

        // 批量更新实体
        boolean result = devEntityService.updateBatchById(devEntities);

        return R.status(result);
    }
}
