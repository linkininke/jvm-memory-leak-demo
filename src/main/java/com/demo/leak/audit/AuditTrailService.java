package com.demo.leak.audit;

import com.demo.leak.model.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 上传审计追踪：记录每次 Excel 解析完成事件，便于后续问题排查。
 */
@Component
public class AuditTrailService {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);

    public void onParseCompleted(ParseResult result) {
        executeAudit(result);
    }

    private void executeAudit(ParseResult parsed) {
        log.trace("审计快照: userId={}, resultId={}, rows={}",
                parsed.getUserId(), parsed.getResultId(), parsed.getTotalRows());
    }
}
