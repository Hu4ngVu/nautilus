package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.io.huangnosimp.nautilusserver.models.Invitation;
import vn.io.huangnosimp.nautilusserver.models.Transfer;
import vn.io.huangnosimp.nautilusserver.services.task.ChangeInvitationStatusJob;
import vn.io.huangnosimp.nautilusserver.services.task.ChangeTransferStatusJob;

@Service
@RequiredArgsConstructor
public class SchedulingServiceImpl implements  SchedulingService {
    private final Scheduler scheduler;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleInvitationExpiration(Invitation invitation) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("invitationId", invitation.getId());

        String jobId = invitation.getId();
        JobDetail jobDetail = JobBuilder.newJob(ChangeInvitationStatusJob.class)
                .withIdentity(jobId)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId)
                .startAt(invitation.getExpiresAt())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule invitation expiration", e);
        }

    }

    @Override
    @TransactionalEventListener(phase =  TransactionPhase.AFTER_COMMIT)
    public void cancelInvitationExpiration(String invitationId) {
        try {
            JobKey jobKey = new JobKey(invitationId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to cancel invitation expiration", e);
        }
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleTransferExpiration(Transfer transfer) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("transferId", transfer.getId());

        String jobId = transfer.getId();
        JobDetail jobDetail = JobBuilder.newJob(ChangeTransferStatusJob.class)
                .withIdentity(jobId)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId)
                .startAt(transfer.getExpiresAt())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule transfer expiration", e);
        }
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cancelTransferExpiration(String transferId) {
        try {
            JobKey jobKey = new JobKey(transferId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to cancel transfer expiration", e);
        }
    }
}
