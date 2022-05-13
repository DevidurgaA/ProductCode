package com.tlc.crm.common.action;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.crm.common.action.res.CrmJsonResponse;
import com.tlc.web.Action;
import com.tlc.web.FullBytesCallback;
import com.tlc.web.WebExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class AbstractCrmAction implements Action, FullBytesCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrmAction.class.getName());

    @Override
    public final void process(WebExchange exchange) {
        if (exchange.isRequestChannelAvailable()) {
            exchange.requestReceiver().readBody(this);
        } else {
            handle(exchange, null);
        }
    }

    public final void handle(final WebExchange exchange, final byte[] body) {
        final CrmJsonResponse crmResponse = new CrmJsonResponse();

        try {
            final CrmRequest crmRequest = new CrmRequest(exchange, body);

            process(crmRequest, crmResponse);
        } catch (ErrorCode errorCode) {
            LOGGER.error("API request failed, Reason : {}", errorCode.getMessage(), errorCode);
            crmResponse.setErrorCode(errorCode);
            crmResponse.put("error", errorCode.getMessage());
        } catch (Throwable throwable) {
            LOGGER.error("Web request Failed, Reason : {}", throwable.getMessage());
            LOGGER.debug("Stack : ", throwable);
            crmResponse.setErrorCode(ErrorCode.get(ErrorCodes.UNKNOWN_ERROR));
        }

        exchange.responseSender().send(crmResponse.getContentType(), crmResponse.getBytes());
    }


    public abstract void process(CrmRequest request, CrmResponse crmResponse) throws Exception;
}
