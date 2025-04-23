package tech.zhiwei.frostmetal.modules.mydata.data;

import com.mongodb.BasicDBObject;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.mongo.CriteriaParser;
import tech.zhiwei.frostmetal.modules.mydata.data.mongo.MultiMongoFactory;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.util.ArrayUtil;
import tech.zhiwei.tool.util.UUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 业务数据 操作类
 *
 * @author LIEN
 * @since 2024/11/25
 */
@Component
public class BizDataDAO {

    @Resource
    private MultiMongoFactory mongoFactory;

    /**
     * 保存单个数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 数据编号，即集合名称
     * @param data     业务数据
     */
    public void insert(String dbCode, String dataCode, Map<String, Object> data) {
        insertBatch(dbCode, dataCode, CollectionUtil.toList(data));
    }

    /**
     * 批量保存数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 数据编号，即集合名称
     * @param dataList 业务数据列表
     */
    public void insertBatch(String dbCode, String dataCode, List<Map<String, Object>> dataList) {
        if (CollectionUtil.isEmpty(dataList)) {
            return;
        }
        dataList.forEach(this::fillData);
        mongoFactory.getTemplate(dbCode).insert(dataList, dataCode);
    }

    /**
     * 根据 多个唯一标识的组合 更新业务数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 业务数据编号
     * @param idMap    唯一标识的组合
     * @param data     业务数据
     */
    public void update(String dbCode, String dataCode, Map<String, Object> idMap, Map<String, Object> data) {
        fillData(data);
        Query query = new Query();
        idMap.forEach((k, v) -> {
            query.addCriteria(Criteria.where(k).is(v));
        });

        Document document = new Document(data);
        Update update = Update.fromDocument(document);
        mongoFactory.getTemplate(dbCode).updateFirst(query, update, dataCode);
    }

    /**
     * 从指定数据库 根据过滤条件 查询指定范围的数据
     *
     * @param dbCode         数据库
     * @param dataCode       数据标识
     * @param bizDataFilters 过滤条件
     * @param skip           跳过数量
     * @param limit          限制数量
     * @return 业务数据列表
     */
    public List<Map<String, Object>> list(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters, String condition, Long skip, Integer limit, BizDataSort... bizDataSorts) {
        MongoTemplate mongoTemplate = mongoFactory.getTemplate(dbCode);
        Query query = new Query();
        if (skip != null) {
            query.skip(skip);
        }
        if (limit != null) {
            query.limit(limit);
        }

        List<Criteria> criteriaList = CollectionUtil.newArrayList();

        // 自定义查询条件
        if (StringUtil.isNotEmpty(condition)) {
            Criteria criteria = CriteriaParser.parse(condition);
            criteriaList.add(criteria);
        }

        // mongodb查询条件集合 加入查询中
        List<Criteria> parsedCriteriaList = parseFilters(bizDataFilters);
        if (CollectionUtil.isNotEmpty(parsedCriteriaList)) {
            criteriaList.addAll(parsedCriteriaList);
        }

        query.addCriteria(new Criteria().andOperator(criteriaList));
        // 排序
        if (ArrayUtil.isNotEmpty(bizDataSorts)) {
            Sort sort = null;
            for (BizDataSort bizDataSort : bizDataSorts) {
                if (sort == null) {
                    sort = Sort.by(bizDataSort.getDirection(), bizDataSort.getName());
                } else {
                    sort = sort.and(Sort.by(bizDataSort.getDirection(), bizDataSort.getName()));
                }
            }
            if (sort != null) {
                query.with(sort);
            }
        }
        query.fields().exclude(MyDataConstant.MONGODB_OBJECT_ID);
        // 执行查询
        List<Document> documents = mongoTemplate.find(query, Document.class, dataCode);
        return new ArrayList<>(documents);
    }

    public List<Map<String, Object>> list(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters, String condition, BizDataSort... bizDataSorts) {
        return this.list(dbCode, dataCode, bizDataFilters, condition, null, null, bizDataSorts);
    }

    /**
     * 分页查询业务数据
     *
     * @param dbCode         数据库
     * @param dataCode       数据标识
     * @param pageNo         当前页数
     * @param pageSize       分页数量
     * @param bizDataFilters 过滤参数
     * @return 业务数据列表
     */
    public List<Map<String, Object>> page(String dbCode, String dataCode, Long pageNo, Integer pageSize, List<BizDataFilter> bizDataFilters, BizDataSort... bizDataSorts) {
        Long skip = (pageNo - 1L) * pageSize;
        return this.list(dbCode, dataCode, bizDataFilters, null, skip, pageSize, bizDataSorts);
    }

    public long total(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters) {
        Query query = new Query();
        List<Criteria> criteriaList = parseFilters(bizDataFilters);
        if (CollectionUtil.isNotEmpty(criteriaList)) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }
        return mongoFactory.getTemplate(dbCode).count(query, dataCode);
    }

    /**
     * 根据 多个唯一标识的组合 查询业务数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 业务数据编号
     * @param idMap    唯一标识组合
     * @return 业务数据
     */
    public Map<String, Object> findByIds(String dbCode, String dataCode, Map<String, Object> idMap) {
        Query query = new Query();
        idMap.forEach((k, v) -> {
            query.addCriteria(Criteria.where(k).is(v));
        });
        return mongoFactory.getTemplate(dbCode).findOne(query, BasicDBObject.class, dataCode);
    }

    /**
     * 根据 MyData唯一标识 查询业务数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 业务数据编号
     * @param bizId    业务数据id
     * @return 业务数据
     */
    public Map<String, Object> findByMdId(String dbCode, String dataCode, String bizId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MyDataConstant.DATA_COLUMN_DATA_ID).is(bizId));
        return mongoFactory.getTemplate(dbCode).findOne(query, BasicDBObject.class, dataCode);
    }

    public void drop(String dbCode, String dataCode) {
        mongoFactory.getTemplate(dbCode).dropCollection(dataCode);
    }

    /**
     * 根据 多个唯一标识的组合 删除业务数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 业务数据编号
     * @param bizId    数据标识
     */
    public void remove(String dbCode, String dataCode, String bizId) {
        Query query = new Query();
        query.addCriteria(Criteria.where(MyDataConstant.DATA_COLUMN_DATA_ID).is(bizId));
        mongoFactory.getTemplate(dbCode).remove(query, dataCode);
    }

    /**
     * 补充数据的 系统字段值
     *
     * @param bizData
     */
    private void fillData(Map<String, Object> bizData) {
        Date currentTime = DateUtil.date();
        // 设置业务数据的最后更新时间
        bizData.put(MyDataConstant.DATA_COLUMN_UPDATE_TIME, currentTime);
        if (ObjectUtil.isEmpty(bizData.get(MyDataConstant.DATA_COLUMN_DATA_ID))) {
            // 设置数据的唯一标识
            bizData.put(MyDataConstant.DATA_COLUMN_DATA_ID, UUID.randomUUID(true).toString(true));
        }
    }

    private List<Criteria> parseFilters(List<BizDataFilter> bizDataFilters) {
        List<Criteria> criteriaList = CollectionUtil.newArrayList();
        // 遍历数据过滤条件
        if (CollectionUtil.isNotEmpty(bizDataFilters)) {
            // mongodb的查询条件集合
            for (BizDataFilter bizDataFilter : bizDataFilters) {
                // 条件key
                final String key = bizDataFilter.getKey();
                // 条件操作
                final String op = bizDataFilter.getOp();
                // 条件值
                final Object value = bizDataFilter.getValue();
                // 条件值类型
                Object type = bizDataFilter.getType();

                Criteria criteria;
                if (MyDataConstant.TASK_FILTER_TYPE_FIELD.equals(type)) {
                    criteria = new Criteria() {
                        @NotNull
                        @Override
                        public Document getCriteriaObject() {
                            String executeOp;
                            switch (op) {
                                case MyDataConstant.DATA_OP_EQ:
                                    executeOp = "==";
                                    break;
                                case MyDataConstant.DATA_OP_NE:
                                case MyDataConstant.DATA_OP_GT:
                                case MyDataConstant.DATA_OP_GTE:
                                case MyDataConstant.DATA_OP_LT:
                                case MyDataConstant.DATA_OP_LTE:
                                    executeOp = op;
                                    break;

                                default:
                                    throw new RuntimeException("BizDataDAO: 不支持的过滤操作");
                            }
                            return new Document("$where", StringUtil.format("this.{}.valueOf() {} this.{}.valueOf()", key, executeOp, value));
                        }
                    };
                } else {
                    // 根据条件操作类型 调用mongodb对应的查询方法
                    criteria = Criteria.where(key);
                    switch (op) {
                        case MyDataConstant.DATA_OP_EQ:
                            criteria.is(value);
                            break;
                        case MyDataConstant.DATA_OP_NE:
                            criteria.ne(value);
                            break;
                        case MyDataConstant.DATA_OP_GT:
                            criteria.gt(value);
                            break;
                        case MyDataConstant.DATA_OP_GTE:
                            criteria.gte(value);
                            break;
                        case MyDataConstant.DATA_OP_LT:
                            criteria.lt(value);
                            break;
                        case MyDataConstant.DATA_OP_LTE:
                            criteria.lte(value);
                            break;
                        case MyDataConstant.DATA_NOT_EMPTY:
                            criteria.ne("");
                            criteriaList.add(Criteria.where(key).ne(null));
                            break;
                        case MyDataConstant.DATA_NOT_NULL:
                            criteria.ne(null).exists(true);
                            break;
                        case MyDataConstant.DATA_OP_LIKE:
                            criteria.regex(".*" + value + ".*", "i");
                            break;

                        default:
                            throw new RuntimeException("BizDataDAO: 不支持的过滤操作");
                    }
                }
                // 存入mongodb的查询条件集合
                criteriaList.add(criteria);
            }
        }
        return criteriaList;
    }
}