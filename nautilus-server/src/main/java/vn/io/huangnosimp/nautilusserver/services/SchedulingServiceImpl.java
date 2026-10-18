package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.io.huangnosimp.nautilusserver.models.Invitation;
import vn.io.huangnosimp.nautilusserver.models.Transfer;
import vn.io.huangnosimp.nautilusserver.services.task.ChangeInvitationStatusJob;
import vn.io.huangnosimp.nautilusserver.services.task.ChangeTransferStatusJob;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingServiceImpl implements  SchedulingService {
    private final Scheduler scheduler;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleInvitationExpiration(Invitation invitation) {
        log.debug("Scheduling expiration for invitation: {}", invitation.getId());
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
        log.info("Scheduled expiration for invitation: {}", invitation.getId());
    }

    @Override
    @TransactionalEventListener(phase =  TransactionPhase.AFTER_COMMIT)
    public void cancelInvitationExpiration(String invitationId) {
        log.debug("Cancelling expiration for invitation: {}", invitationId);
        try {
            JobKey jobKey = new JobKey(invitationId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to cancel invitation expiration", e);
        }
        log.info("Cancelled expiration for invitation: {}", invitationId);
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleTransferExpiration(Transfer transfer) {
        log.debug("Scheduling expiration for transfer: {}", transfer.getId());
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
        log.info("Scheduled expiration for transfer: {}", transfer.getId());
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cancelTransferExpiration(String transferId) {
        log.debug("Cancelling expiration for transfer: {}", transferId);
        try {
            JobKey jobKey = new JobKey(transferId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to cancel transfer expiration", e);
        }
        log.info("Cancelled expiration for transfer: {}", transferId);
    }
}
