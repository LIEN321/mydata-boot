package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.util;

import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.util.ArrayUtil;

import static tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant.SINGLE_OPERATOR;

/**
 * 流水线工具类
 *
 * @author LIEN
 * @since 2025/12/19
 */
public class PipelineUtil {
    /**
     * 判断指定对象是否为空<br/>
     * 若为MyData流水线对象(PipelineBizData) 则判断业务数据是否为空<br/>
     * 否则通用判断对象是否为空 {@link ObjectUtil#isEmpty(Object)}
     *
     * @param object 被判断的对象
     * @return true-空，false-不空
     */
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof PipelineBizData) {
            return CollectionUtil.isEmpty(((PipelineBizData) object).getBizData());
        }

        return ObjectUtil.isEmpty(object);
    }

    /**
     * 判断指定对象是否为非空
     * {@link PipelineUtil#isEmpty(Object)}
     *
     * @param object 被判断的对象
     * @return true-非空，false-空
     */
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * 判断对象是否符合条件 o1 op o2
     *
     * @param o1       对象1
     * @param operator 条件
     * @param o2       对象2
     * @return 对比结果
     */
    public static boolean compare(Object o1, String operator, Object o2) {

        if (ArrayUtil.contains(SINGLE_OPERATOR, operator)) {
            return switch (operator) {
                // not null
                case MyDataConstant.CONDITION_NOT_NULL -> ObjectUtil.isNotNull(o1);
                // not empty
                case MyDataConstant.CONDITION_NOT_EMPTY -> PipelineUtil.isNotEmpty(o1);
                // is null
                case MyDataConstant.CONDITION_IS_NULL -> ObjectUtil.isNull(o1);
                // is empty
                case MyDataConstant.CONDITION_IS_EMPTY -> PipelineUtil.isEmpty(o1);

                default -> throw new IllegalStateException("Unexpected value: " + operator);
            };
        }

        // 判断业务数据值 和 过滤数据值 都可对比，否则过滤条件无效
        if (!(o1 instanceof Comparable && o2 instanceof Comparable)) {
            throw new IllegalArgumentException(StringUtil.format("条件无效：{}或{} 无法进行对比", o1, o2));
        }

        if (o1.getClass() != o2.getClass()) {
            throw new IllegalArgumentException(StringUtil.format("条件值 {}({}) 与 {}({}) 类型不同，无法进行对比", o1, o1.getClass().getSimpleName(), o2, o2.getClass().getSimpleName()));
        }

        Comparable c1 = (Comparable) o1;
        Comparable c2 = (Comparable) o2;

        return switch (operator) {
            // 等于
            case MyDataConstant.CONDITION_EQ -> (ObjectUtil.compare(c1, c2) == 0);
            // 不等于
            case MyDataConstant.CONDITION_NE -> (ObjectUtil.compare(c1, c2) != 0);
            // 大于
            case MyDataConstant.CONDITION_GT -> (ObjectUtil.compare(c1, c2) > 0);
            // 大于等于
            case MyDataConstant.CONDITION_GTE -> (ObjectUtil.compare(c1, c2) >= 0);
            // 小于
            case MyDataConstant.CONDITION_LT -> (ObjectUtil.compare(c1, c2) < 0);
            // 小于等于
            case MyDataConstant.CONDITION_LTE -> (ObjectUtil.compare(c1, c2) <= 0);

            default -> throw new IllegalArgumentException(
                    StringUtil.format("过滤条件无效: 不支持的过滤操作 {}", operator)
            );
        };
    }
}
