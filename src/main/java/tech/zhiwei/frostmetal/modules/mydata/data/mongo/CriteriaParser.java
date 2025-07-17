package tech.zhiwei.frostmetal.modules.mydata.data.mongo;

import jakarta.validation.constraints.NotNull;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriteriaParser {

    // 操作符优先级映射
    private static final Map<String, Integer> OPERATOR_PRECEDENCE;

    static {
        OPERATOR_PRECEDENCE = MapUtil.newHashMap();
        OPERATOR_PRECEDENCE.put("OR", 1);
        OPERATOR_PRECEDENCE.put("AND", 2);
        OPERATOR_PRECEDENCE.put("=", 3);
        OPERATOR_PRECEDENCE.put("!=", 3);
        OPERATOR_PRECEDENCE.put(">", 3);
        OPERATOR_PRECEDENCE.put("<", 3);
        OPERATOR_PRECEDENCE.put(">=", 3);
        OPERATOR_PRECEDENCE.put("<=", 3);
        OPERATOR_PRECEDENCE.put("LIKE", 3);
        OPERATOR_PRECEDENCE.put("IS NULL", 3);
        OPERATOR_PRECEDENCE.put("IS NOT NULL", 3);
        OPERATOR_PRECEDENCE.put("IS EMPTY", 3);
    }

    ;

    /**
     * 解析条件字符串为Criteria对象
     *
     * @param conditionStr 条件字符串，如 "(A = value1 OR B = value2) AND C = value3"
     */
    public static Criteria parse(String conditionStr) {
        List<String> tokens = tokenize(conditionStr);
        return buildCriteria(tokens);
    }

    // ====================== 词法分析 ======================
    private static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "(?i)(\\w+\\s+IS\\s+(NOT\\s+)?(NULL|EMPTY)|" +  // IS [NOT] NULL/EMPTY
                        ">=|<=|!=|=|>|<|" +                        // 比较操作符
                        "LIKE|AND|OR|" +                           // 其他操作符
                        "\\w+|" +                                  // 字段名
                        "'[^']*'|\"[^\"]*\"|" +                    // 带引号的字符串
                        "\\d+\\.?\\d*|" +                          // 数字
                        "\\(|\\))"                                  // 括号
        );

        Matcher matcher = pattern.matcher(input.replaceAll("\\s+", " "));

        while (matcher.find()) {
            String token = matcher.group().trim();
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    // ====================== 语法解析 ======================
    private static Criteria buildCriteria(List<String> tokens) {
        Stack<Criteria> output = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            // 跳过已处理的token
            if (token == null) {
                continue;
            }
            if ("(".equalsIgnoreCase(token)) {
                operators.push(token);
            } else if (")".equalsIgnoreCase(token)) {
                processClosingBracket(output, operators);
            } else if (isOperator(token)) {
                processOperator(token, output, operators);
            } else {
                output.push(parseCondition(token, tokens, output));
            }
        }

        while (!operators.isEmpty()) {
            applyOperator(output, operators.pop());
        }
        return output.pop();
    }

    // ====================== 操作符处理 ======================
    private static boolean isOperator(String token) {
        return OPERATOR_PRECEDENCE.containsKey(token.toUpperCase());
    }

    private static void processOperator(String op, Stack<Criteria> output, Stack<String> operators) {
        while (!operators.isEmpty() &&
                !"(".equals(operators.peek()) &&
                OPERATOR_PRECEDENCE.get(op.toUpperCase()) <= OPERATOR_PRECEDENCE.get(operators.peek())) {
            applyOperator(output, operators.pop());
        }
        operators.push(op.toUpperCase());
    }

    private static void processClosingBracket(Stack<Criteria> output, Stack<String> operators) {
        while (!"(".equals(operators.peek())) {
            applyOperator(output, operators.pop());
        }
        operators.pop(); // 移除 '('
    }

    private static void applyOperator(Stack<Criteria> output, String op) {
        if ("AND".equals(op) || "OR".equals(op)) {
            Criteria right = output.pop();
            Criteria left = output.pop();
            output.push("AND".equals(op) ?
                    new Criteria().andOperator(left, right) :
                    new Criteria().orOperator(left, right)
            );
        }
    }

    // ====================== 条件转换 ======================
    private static Criteria parseCondition(String token, List<String> tokens, Stack<Criteria> output) {
        // 检查是否为普通字段名
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) { // 字段名
            int index = tokens.indexOf(token);
            if (index < tokens.size() - 1) {
                String op = tokens.get(index + 1);

                // 检查是否是比较表达式结束
                if (index + 2 >= tokens.size() ||
                        tokens.get(index + 2).matches("AND|OR|\\)")) {
                    throw new IllegalArgumentException("Incomplete condition after: " + token + " " + op);
                }

                String rightValue = tokens.get(index + 2);
                boolean isFieldRef = rightValue.matches("^[A-Za-z_][A-Za-z0-9_]*$")
                        && !Arrays.asList("AND", "OR", "IS").contains(rightValue.toUpperCase());

                // 标记已处理的token
                tokens.set(index, null);     // 标记field1已处理
                tokens.set(index + 1, null); // 标记操作符已处理
                tokens.set(index + 2, null); // 标记field2已处理

                return buildSingleCriteria(token, op, rightValue, isFieldRef);
            }
        }
        // 检查是否为复合条件（IS NOT NULL/IS EMPTY）
        if (token.matches("(?i)^\\w+\\s+IS\\s+(NOT\\s+)?(NULL|EMPTY)$")) {
            String[] parts = token.split("\\s+");
            String field = parts[0];
            String operator = parts.length == 3 ?
                    "IS " + parts[2] :  // IS NULL/IS EMPTY
                    "IS NOT " + parts[3]; // IS NOT NULL/IS NOT EMPTY
            return buildSingleCriteria(field, operator, "", false);
        }
        throw new IllegalArgumentException("Invalid condition format: " + token);
    }

    private static Criteria buildSingleCriteria(String field, String operator, String rightValue, boolean isFieldRef) {
        operator = operator.toUpperCase();

        if (isFieldRef) {
            // 处理字段引用比较
            String executeOp;
            switch (operator) {
                case MyDataConstant.CONDITION_EQ:
                    executeOp = "==";
                    break;
                case MyDataConstant.CONDITION_NE:
                case MyDataConstant.CONDITION_GT:
                case MyDataConstant.CONDITION_GTE:
                case MyDataConstant.CONDITION_LT:
                case MyDataConstant.CONDITION_LTE:
                    executeOp = operator;
                    break;

                default:
                    throw new RuntimeException("BizDataDAO: 不支持的过滤操作");
            }
            return new Criteria() {
                @NotNull
                @Override
                public Document getCriteriaObject() {
                    return new Document("$where", StringUtil.format("this.{}.valueOf() {} this.{}.valueOf()", field, executeOp, rightValue));
                }
            };
        }

        Object value = parseValue(rightValue);
        switch (operator) {
            case "=":
                return Criteria.where(field).is(value);
            case "!=":
                return Criteria.where(field).ne(value);
            case ">":
                return Criteria.where(field).gt(value);
            case "<":
                return Criteria.where(field).lt(value);
            case ">=":
                return Criteria.where(field).gte(value);
            case "<=":
                return Criteria.where(field).lte(value);
            case "LIKE":
                return Criteria.where(field).regex(((String) value).replace("%", ".*"), "i");
            case "IS NULL":
                return Criteria.where(field).exists(false).is(null);
            case "IS NOT NULL":
                return Criteria.where(field).exists(true).ne(null);
            case "IS EMPTY":
                return new Criteria().orOperator(
                        Criteria.where(field).is(""),
                        Criteria.where(field).is(Collections.emptyList()),
                        Criteria.where(field).exists(false)
                );
            case "IS NOT EMPTY":
                return new Criteria().andOperator(
                        Criteria.where(field).ne(""),
                        Criteria.where(field).ne(Collections.emptyList()),
                        Criteria.where(field).exists(true)
                );
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    private static Object parseValue(String value) {
        if (value.startsWith("'") || value.startsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}