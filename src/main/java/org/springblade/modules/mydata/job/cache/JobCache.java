package org.springblade.modules.mydata.job.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.utils.RedisUtil;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Job缓存类
 *
 * @author LIEN
 * @since 2022/7/16
 */
@Component
@AllArgsConstructor
public class JobCache {

    private final RedisUtil redisUtil;

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String CACHE_TASK = "mydata:task:";

    public static final String CACHE_JOB = "mydata:job:";

    /**
     * 删除job相关缓存
     */
    public void removeAll() {
        Set<String> taskKeys = redisTemplate.keys(CACHE_TASK + "*");
        if (CollUtil.isNotEmpty(taskKeys)) {
            redisTemplate.delete(taskKeys);
        }
        Set<String> jobKeys = redisTemplate.keys(CACHE_JOB + "*");
        if (CollUtil.isNotEmpty(jobKeys)) {
            redisTemplate.delete(jobKeys);
        }
    }

    /**
     * 缓存任务
     *
     * @param taskInfo 任务对象
     * @throws IllegalArgumentException 缓存时长无效
     */
    public void cacheJob(TaskInfo taskInfo) throws IllegalArgumentException {
        // 计算任务缓存有效时长
        long expire = DateUtil.between(taskInfo.getStartTime(), taskInfo.getNextRunTime(), DateUnit.SECOND);
        if (expire <= 0) {
            throw new IllegalArgumentException(StrUtil.format("expire <= 0秒, startTime = {}, nextRunTime = {}"
                    , DateUtil.format(taskInfo.getStartTime(), DatePattern.NORM_DATETIME_PATTERN)
                    , DateUtil.format(taskInfo.getNextRunTime(), DatePattern.NORM_DATETIME_PATTERN)));
        }

        taskInfo.appendLog("任务存入redis，缓存时长 {} 秒", expire);
        redisUtil.set(CACHE_TASK + taskInfo.getId(), taskInfo);
        redisUtil.set(CACHE_JOB + taskInfo.getId(), taskInfo.getId(), expire);
    }

    /**
     * 从缓存获取任务
     *
     * @param taskId 任务id
     * @return 任务
     */
    public TaskInfo getTask(String taskId) {
        return (TaskInfo) redisUtil.get(CACHE_TASK + taskId);
    }

    /**
     * 从缓存获取任务 下次执行定时job
     *
     * @param taskId 任务id
     * @return 下次执行的定时job
     */
    public Long getJob(String taskId) {
        return (Long) redisUtil.get(CACHE_JOB + taskId);
    }

    /**
     * 删除指定任务的缓存
     *
     * @param taskId 任务id
     */
    public void removeTask(Long taskId) {
        redisUtil.del(CACHE_TASK + taskId);
        redisUtil.del(CACHE_JOB + taskId);
    }
}
