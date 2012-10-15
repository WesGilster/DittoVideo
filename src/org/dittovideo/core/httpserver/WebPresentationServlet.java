package org.dittovideo.core.httpserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dittovideo.core.server.DittoVideo;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.LocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPresentationServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebPresentationServlet.class);
	private static final long serialVersionUID = 7249073283440105706L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}
	
	private void doAll(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getPathInfo().substring(1).equals(DittoVideo.getSetup().webPresentationPage)) {
				gotoHomePage(request, response);
			} else {
				//TODO: do other page requests...
			}
		} catch (InterruptedException e) {
			LOGGER.error("UPNP Server interrupted during setup", e);
		}
	}
	
	private void gotoHomePage(HttpServletRequest request, HttpServletResponse response) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("<html><head><b>");
			Action browseAction = null;
			for (LocalService service : DittoVideo.getDittoServer().getMediaServer().getRoot().findServices()) {
				Action action = service.getAction("Browse");

				if (action == null)
					continue;
				
				browseAction = action;
			}
			
			ActionInvocation invocation = new ActionInvocation(browseAction);
			ActionArgument arg = browseAction.getInputArgument("ObjectID");
			invocation.setInput(new ActionArgumentValue(arg, "0"));
			arg = browseAction.getInputArgument("BrowseFlag");
			invocation.setInput(new ActionArgumentValue(arg, "BrowseDirectChildren"));
			arg = browseAction.getInputArgument("StartingIndex");
			invocation.setInput(new ActionArgumentValue(arg, "0"));
			arg = browseAction.getInputArgument("RequestedCount");
			invocation.setInput(new ActionArgumentValue(arg, "10000"));
			new ActionCallback.Default(invocation, DittoVideo.getDittoServer().getControlPoint()).run();
			for (ActionArgumentValue value : invocation.getOutput()) {
				builder.append(value.getArgument());
				builder.append("=");
				builder.append(value.getValue());
				builder.append("</br>");
			}
			builder.append("</b>TODO: Start building the tree from the above nodes!!  YEAH!</head></html>");//TODO: Buid the nodes HERE!!
			response.getOutputStream().write(builder.toString().getBytes());
		} catch (IOException e) {
			LOGGER.error("Couldn't write to output", e);
		}
	}
}
