package tech.zhiwei.frostmetal.modules.mydata.schedule.quartz;

import lombok.SneakyThrows;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 租户流水线的执行队列
 *
 * @author LIEN
 * @since 2025/1/15
 */
public class TenantJobListener implements JobListener {
    private final ConcurrentHashMap<String, BlockingQueue<JobExecutionContext>> tenantJobQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> tenantThreadCounters = new ConcurrentHashMap<>();
    private final int maxThreadsPerTenant;

    public TenantJobListener(int maxThreadsPerTenant) {
        this.maxThreadsPerTenant = maxThreadsPerTenant;
    }

    @Override
    public String getName() {
        return "TenantAwareJobListener";
    }

    @SneakyThrows
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String tenantId = context.getJobDetail().getJobDataMap().getString(SysConstant.TENANT_ID);
        if (tenantId == null) {
            return; // 无租户信息的任务，不限制
        }

        tenantThreadCounters.putIfAbsent(tenantId, new AtomicInteger(0));
        tenantJobQueues.putIfAbsent(tenantId, new LinkedBlockingQueue<>());

        AtomicInteger currentCount = tenantThreadCounters.get(tenantId);

        if (currentCount.get() >= maxThreadsPerTenant) {
            // 达到限制，任务进入等待队列
            try {
                tenantJobQueues.get(tenantId).put(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JobExecutionException("Failed to enqueue job for tenant: " + tenantId, e);
            }

            // 阻止当前任务执行，等待后续调度
            throw new JobExecutionException(false);
        } else {
            // 增加当前租户的活跃线程计数
            currentCount.incrementAndGet();
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String tenantId = context.getJobDetail().getJobDataMap().getString(SysConstant.TENANT_ID);
        if (tenantId == null) {
            return; // 无租户信息的任务，不限制
        }

        AtomicInteger currentCount = tenantThreadCounters.get(tenantId);
        if (currentCount != null) {
            // 减少活跃线程计数
            currentCount.decrementAndGet();

            // 从等待队列中取出任务并手动触发执行
            BlockingQueue<JobExecutionContext> queue = tenantJobQueues.get(tenantId);
            if (queue != null && !queue.isEmpty()) {
                JobExecutionContext nextJob = queue.poll();
                if (nextJob != null) {
                    try {
                        Scheduler scheduler = nextJob.getScheduler();
                        scheduler.triggerJob(nextJob.getJobDetail().getKey(), nextJob.getMergedJobDataMap());
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
