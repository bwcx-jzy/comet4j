package org.comet4j.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;

/**
 * 消息连接器，负责与客户端建立连接
 */
public class CometServlet extends HttpServlet implements CometProcessor {

	private static final long serialVersionUID = 1L;

	public CometServlet() {
		super();
	}

	/*
	 * @see
	 * org.apache.catalina.CometProcessor#event(org.apache.catalina.CometEvent)
	 */
	public void event(CometEvent event) throws IOException, ServletException {
		HttpServletRequest request = event.getHttpServletRequest();
		HttpServletResponse response = event.getHttpServletResponse();
		request.setAttribute("org.apache.tomcat.comet.timeout", CometContext.getInstance().getTimeout());
		event.setTimeout(CometContext.getInstance().getTimeout());
		if (event.getEventType() == CometEvent.EventType.BEGIN) {
			String action = request.getParameter(CometProtocol.FLAG_ACTION);

			if (CometProtocol.CMD_CONNECT.equals(action)) {

				CometContext.getInstance().getEngine().connect(request, response);
			} else if (CometProtocol.CMD_REVIVAL.equals(action)) {

				CometContext.getInstance().getEngine().revival(request, response);

			} else if (CometProtocol.CMD_DROP.equals(action)) {
				CometContext.getInstance().getEngine().drop(request, response);
			}
		} else if (event.getEventType() == CometEvent.EventType.ERROR) {
			if (event.getEventSubType() == CometEvent.EventSubType.TIMEOUT) {
				CometContext.getInstance().getEngine().dying(request, response);
				// CometContext.getInstance().log("dying:" +
				// request.getParameter("cid"));
			} else {

				CometContext.getInstance().getEngine().drop(request, response);
			}

		} else if (event.getEventType() == CometEvent.EventType.END) {
			String action = event.getHttpServletRequest().getParameter(CometProtocol.FLAG_ACTION);

			// CometContext.getInstance().getEngine().dying(request, response);

		} else if (event.getEventType() == CometEvent.EventType.READ) {

		}

	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
