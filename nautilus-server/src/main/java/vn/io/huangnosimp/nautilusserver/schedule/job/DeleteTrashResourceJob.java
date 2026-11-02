package vn.io.huangnosimp.nautilusserver.schedule.job;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import vn.io.huangnosimp.nautilusserver.events.DeleteTrashResourceEvent;

@Component
@RequiredArgsConstructor
public class DeleteTrashResourceJob extends QuartzJobBean {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        eventPublisher.publishEvent(new DeleteTrashResourceEvent());
    }
}
