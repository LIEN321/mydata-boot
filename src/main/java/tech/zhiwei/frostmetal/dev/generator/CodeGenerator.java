package tech.zhiwei.frostmetal.dev.generator;

import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.extra.template.engine.freemarker.FreemarkerEngine;
import tech.zhiwei.frostmetal.core.constant.DevConstant;
import tech.zhiwei.frostmetal.dev.generator.bean.Entity;
import tech.zhiwei.frostmetal.dev.generator.bean.Property;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代码生成器
 *
 * @author LIEN
 * @since 2024/9/29
 */
public class CodeGenerator {

    /**
     * 生成代码
     *
     * @param entity       实体及属性配置
     * @param auth         作者信息
     * @param javaCodePath java代码路径
     * @param uiCodePath   ui代码路径
     */
    public static void generate(Entity entity, String auth, String javaCodePath, String uiCodePath) {

        List<Property> properties = entity.getProperties();
        // 需要导入的类
        Set<String> classes = properties.stream()
                .map(Property::getTypeClassFullName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 需要导入的组件
        Set<String> components = properties.stream()
                .map(Property::getComponentName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Object> map = BeanUtil.beanToMap(entity);
        map.put("auth", auth);
        map.put("date", DateUtil.TODAY_DATETIME_FORMAT.format(new Date()));
        map.put("classes", classes);
        map.put("components", components);
        map.put("SEARCH_TYPE", DevConstant.SEARCH_TYPE);

        // 设置模板配置
        TemplateConfig templateConfig = new TemplateConfig();
        // 指定模板路径 在classpath/templates
        templateConfig.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
        templateConfig.setPath("templates");
        // 指定模板引擎为 freemarker
        templateConfig.setCustomEngine(FreemarkerEngine.class);

        // 初始模板引擎
        TemplateEngine templateEngine = TemplateUtil.createEngine(templateConfig);

        // ---------- api代码 ----------
        File javaDir = new File(javaCodePath);
        // 根据package创建目录结构
        File codeDir = new File(javaDir, StringUtil.replace(entity.getPackageName(), ".", File.separator));
        codeDir.mkdirs();

        // controller
        Template templateController = templateEngine.getTemplate("api/controller.java.ftl");
        templateController.render(map,
                new File(codeDir, "controller/" + entity.getEntityClassName() + "Controller.java"));
        // service
        Template templateIService = templateEngine.getTemplate("api/iservice.java.ftl");
        templateIService.render(map, new File(codeDir, "service/I" + entity.getEntityClassName() + "Service.java"));
        Template templateService = templateEngine.getTemplate("api/service.java.ftl");
        templateService.render(map, new File(codeDir, "service/impl/" + entity.getEntityClassName() + "Service.java"));
        // mapper
        Template templateMapper = templateEngine.getTemplate("api/mapper.java.ftl");
        templateMapper.render(map, new File(codeDir, "mapper/" + entity.getEntityClassName() + "Mapper.java"));
        // entity
        Template templateEntity = templateEngine.getTemplate("api/entity.java.ftl");
        templateEntity.render(map, new File(codeDir, "entity/" + entity.getEntityClassName() + ".java"));
        // dto
        Template templateDTO = templateEngine.getTemplate("api/dto.java.ftl");
        templateDTO.render(map, new File(codeDir, "dto/" + entity.getEntityClassName() + "DTO.java"));
        // vo
        Template templateVO = templateEngine.getTemplate("api/vo.java.ftl");
        templateVO.render(map, new File(codeDir, "vo/" + entity.getEntityClassName() + "VO.java"));
        // wrapper
        Template templateWrapper = templateEngine.getTemplate("api/wrapper.java.ftl");
        templateWrapper.render(map, new File(codeDir, "wrapper/" + entity.getEntityClassName() + "Wrapper.java"));

        // ---------- mysql脚本 ----------
        File sqlDir = new File(javaDir, "sql");
        sqlDir.mkdirs();
        Template mysqlTemplate = templateEngine.getTemplate("sql/mysql.ftl");
        mysqlTemplate.render(map, new File(sqlDir, entity.getEntityCode() + "_mysql.sql"));

        // ---------- ui代码 ----------
        // 根据package创建目录结构
        File pageDir = new File(uiCodePath, entity.getEntityClassName());
        pageDir.mkdirs();
        Template templatePage = templateEngine.getTemplate("page/page.tsx.ftl");
        templatePage.render(map, new File(pageDir, entity.getEntityClassName() + ".tsx"));
    }
}
