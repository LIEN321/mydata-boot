package org.springblade.modules.mydata.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springblade.common.constant.MdConstant;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 业务数据 操作类
 *
 * @author LIEN
 * @since 2022/7/13
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
        mongoFactory.getTemplate(dbCode).insert(data, dataCode);
    }

    /**
     * 批量保存数据
     *
     * @param dbCode   数据库编号
     * @param dataCode 数据编号，即集合名称
     * @param dataList 业务数据列表
     */
    public void insertBatch(String dbCode, String dataCode, List<Map<String, Object>> dataList) {
        mongoFactory.getTemplate(dbCode).insert(dataList, dataCode);
    }

    public void update(String dbCode, String dataCode, String idField, String idValue, Map<String, Object> data) {
        Query query = new Query(Criteria.where(idField).is(idValue));
        Document document = new Document(data);
        Update update = Update.fromDocument(document);
        mongoFactory.getTemplate(dbCode).updateFirst(query, update, dataCode);
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
        Query query = new Query();
        idMap.forEach((k, v) -> {
            query.addCriteria(Criteria.where(k).is(v));
        });

        Document document = new Document(data);
        Update update = Update.fromDocument(document);
        mongoFactory.getTemplate(dbCode).updateFirst(query, update, dataCode);
    }

    public List<Map> listAll(String dbCode, String dataCode) {
        return mongoFactory.getTemplate(dbCode).findAll(Map.class, dataCode);
    }

    public List<Map> list(String dbCode, String dataCode, int size) {
        Assert.isTrue(size >= 0);
        Query query = new Query();
        query.limit(size);
        return mongoFactory.getTemplate(dbCode).find(query, Map.class, dataCode);
    }

    public List<Map> list(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters) {
        return list(dbCode, dataCode, bizDataFilters, null, null);
    }

    public List<Map> list(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters, Long skip, Integer limit) {
        MongoTemplate mongoTemplate = mongoFactory.getTemplate(dbCode);
        Query query = new Query();
        if (skip != null) {
            query.skip(skip);
        }
        if (limit != null) {
            query.limit(limit);
        }
        // 遍历数据过滤条件
        if (CollUtil.isNotEmpty(bizDataFilters)) {
            // mongodb的查询条件集合
            List<Criteria> criteriaList = CollUtil.newArrayList();
            for (BizDataFilter bizDataFilter : bizDataFilters) {
                // 条件key
                String key = bizDataFilter.getKey();
                // 条件操作
                String op = bizDataFilter.getOp();
                // 条件值
                Object value = bizDataFilter.getValue();
                // 条件值类型
                Object type = bizDataFilter.getType();

                Criteria criteria;
                if (MdConstant.TASK_FILTER_TYPE_FIELD.equals(type)) {
                    criteria = new Criteria() {
                        @NotNull
                        @Override
                        public Document getCriteriaObject() {
                            String executeOp;
                            switch (op) {
                                case MdConstant.DATA_OP_EQ:
                                    executeOp = "==";
                                    break;
                                case MdConstant.DATA_OP_NE:
                                case MdConstant.DATA_OP_GT:
                                case MdConstant.DATA_OP_GTE:
                                case MdConstant.DATA_OP_LT:
                                case MdConstant.DATA_OP_LTE:
                                    executeOp = op;
                                    break;

                                default:
                                    throw new RuntimeException("BizDataDAO: 不支持的过滤操作");
                            }
                            return new Document("$where", "this.trade_quantity " + executeOp + " this.woo_quantity");
                        }
                    };
                } else {
                    // 根据条件操作类型 调用mongodb对应的查询方法
                    criteria = Criteria.where(key);
                    switch (op) {
                        case MdConstant.DATA_OP_EQ:
                            criteria.is(value);
                            break;
                        case MdConstant.DATA_OP_NE:
                            criteria.ne(value);
                            break;
                        case MdConstant.DATA_OP_GT:
                            criteria.gt(value);
                            break;
                        case MdConstant.DATA_OP_GTE:
                            criteria.gte(value);
                            break;
                        case MdConstant.DATA_OP_LT:
                            criteria.lt(value);
                            break;
                        case MdConstant.DATA_OP_LTE:
                            criteria.lte(value);
                            break;
                        case MdConstant.DATA_NOT_EMPTY:
                            criteria.ne("");
                            criteriaList.add(Criteria.where(key).ne(null));
                            break;
                        case MdConstant.DATA_NOT_NULL:
                            criteria.ne(null).exists(true);
                            break;

                        default:
                            throw new RuntimeException("BizDataDAO: 不支持的过滤操作");
                    }
                }
                // 存入mongodb的查询条件集合
                criteriaList.add(criteria);
            }

            // mongodb查询条件集合 加入查询中
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        // 执行查询
        return mongoTemplate.find(query, Map.class, dataCode);
    }

    public List<Map> page(String dbCode, String dataCode, int pageNo, int pageSize) {
        Query query = new Query();
        query.skip((pageNo - 1) * pageSize);
        query.limit(pageSize);
        return mongoFactory.getTemplate(dbCode).find(query, Map.class, dataCode);
    }

    public long total(String dbCode, String dataCode) {
        Query query = new Query();
        return mongoFactory.getTemplate(dbCode).count(query, dataCode);
    }

    public Map<String, Object> findById(String dbCode, String dataCode, String idCode, Object idValue) {
        Query query = new Query(Criteria.where(idCode).is(idValue));
        return mongoFactory.getTemplate(dbCode).findOne(query, BasicDBObject.class, dataCode);
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

    public void drop(String dbCode, String dataCode) {
        mongoFactory.getTemplate(dbCode).dropCollection(dataCode);
    }

}
