package org.dittovideo.core.httpserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectStreamingServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectStreamingServlet.class);
	private static final long serialVersionUID = 5110548757293069069L;
	
	private Hashtable<String, File> servableFiles = new Hashtable<String, File>();
	
	public DirectStreamingServlet() {
	}

	public void serveFile(String id, File servedFile) {
		servableFiles.put(id,  servedFile);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAll(request, response);
	}
	
	private void doAll(HttpServletRequest request, HttpServletResponse response) {
		File servedFile = servableFiles.get(request.getPathInfo().substring(1));
		if (servedFile == null) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				LOGGER.info("Couldn't send error back to client for path:" + request.getPathInfo(), e);
			}
			return;
		}
		
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(servedFile));
			//TODO: Not having the proper headers and streaming caps here is causing the renderer to close the connection prematurely during copy 
			IOUtils.copy(stream, response.getOutputStream());
		} catch (IOException e)  {
			LOGGER.info("Can't stream file:" + servedFile, e);
		} finally {
			if (stream != null)
				try {stream.close();} catch (IOException e) {}
		}
	}
}
