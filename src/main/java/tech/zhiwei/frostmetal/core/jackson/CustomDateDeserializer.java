package tech.zhiwei.frostmetal.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

    // 定义多个日期格式的列表
    private static final List<SimpleDateFormat> dateFormats = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            , new SimpleDateFormat("yyyy-MM-dd")
            , new SimpleDateFormat("yyyy/MM/dd")
            , new SimpleDateFormat("dd-MM-yyyy")
            , new SimpleDateFormat("MM-dd-yyyy")
    );

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        String dateStr = jsonParser.getText();  // 获取传入的日期字符串

        // 尝试使用每个日期格式解析
        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateStr);
            } catch (ParseException e) {
                // 如果当前格式解析失败，继续尝试下一个格式
            }
        }
        // 如果所有格式都无法解析，则抛出异常
        throw new RuntimeException("Invalid date format: " + dateStr);
    }
}
