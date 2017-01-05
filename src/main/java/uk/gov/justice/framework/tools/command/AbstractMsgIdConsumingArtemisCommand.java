package uk.gov.justice.framework.tools.command;

import com.beust.jcommander.Parameter;

public class AbstractMsgIdConsumingArtemisCommand extends AbstractArtemisCommand {

    @Parameter(names = "-msgId")
    String msgId;

    protected boolean singleMessageIdProvided() {
        return msgId != null && !"".equals(msgId.trim());
    }
}
