package vn.io.huangnosimp.nautilusserver.services.task;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeInvitationStatusJob extends QuartzJobBean {
    private final ChangeStatusService changeStatusService;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String invitationId = dataMap.getString("invitationId");
        changeStatusService.expireInvitation(invitationId);
    }
}
