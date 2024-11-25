package tech.zhiwei.frostmetal.core.jackson;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import tech.zhiwei.tool.lang.StringPool;
import tech.zhiwei.tool.util.ArrayUtil;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * MybatisPlus 类型转换：String[] 与 "x,y,z,..."格式的转换
 *
 * @author LIEN
 * @since 2024/10/10
 */
public class IntegerArrayTypeHandler extends BaseTypeHandler<Integer[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Integer[] parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            // 将数组转换为逗号分隔的字符串
            ps.setString(i, ArrayUtil.join(parameter, StringPool.COMMA));
        } else {
            ps.setString(i, "");
        }
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parseStringToIntegerArray(value);
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parseStringToIntegerArray(value);
    }

    @Override
    public Integer[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parseStringToIntegerArray(value);
    }

    private Integer[] parseStringToIntegerArray(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Arrays.stream(value.split(StringPool.COMMA))
                .map(Integer::parseInt)
                .toArray(Integer[]::new);
    }
}
