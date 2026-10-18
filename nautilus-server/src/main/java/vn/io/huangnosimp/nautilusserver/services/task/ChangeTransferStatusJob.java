package vn.io.huangnosimp.nautilusserver.services.task;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeTransferStatusJob extends QuartzJobBean {
    private final ChangeStatusService changeStatusService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String transferId = context.getMergedJobDataMap().getString("transferId");
        changeStatusService.expireTransfer(transferId);
    }
}
