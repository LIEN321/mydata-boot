package tech.zhiwei.frostmetal.core.jackson;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MybatisPlus 类型转换：String[] 与 "x,y,z,..."格式的转换
 *
 * @author LIEN
 * @since 2024/10/10
 */
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            // 将数组转换为逗号分隔的字符串
            ps.setString(i, String.join(",", parameter));
        } else {
            ps.setString(i, "");
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        // 将逗号分隔的字符串转换为数组
        return value != null ? value.split(",") : new String[0];
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? value.split(",") : new String[0];
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? value.split(",") : new String[0];
    }
}
