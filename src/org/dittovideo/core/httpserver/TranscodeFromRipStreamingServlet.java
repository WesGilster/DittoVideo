package org.dittovideo.core.httpserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscodeFromRipStreamingServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranscodeFromRipStreamingServlet.class);
	private static final long serialVersionUID = 6818711004223811897L;
	private List<Process> openStreams = new ArrayList<Process>();
	
	private Hashtable<String, File> servableFiles = new Hashtable<String, File>();
	
	public TranscodeFromRipStreamingServlet() {
	}

	public void serveFile(String id, File servedFile) {
		servableFiles.put(id,  servedFile);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doAll(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doAll(req, resp);
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
		
		ProcessBuilder builder = new ProcessBuilder("ffmpeg -i input -f format tcp://hostname:port?listen");
		Socket socket = null;
		Process newFFMPegProcess = null;
		try {
			newFFMPegProcess = builder.start();
			socket = new Socket();
			BufferedInputStream socketStream = new BufferedInputStream(socket.getInputStream());
				
			IOUtils.copy(socketStream, response.getOutputStream());
		} catch (IOException e) {
			LOGGER.error("Couldn't stream:" + servedFile, e);
		} finally {
			if (socket != null)
				try {socket.close();} catch (IOException e) {}
			
			if (newFFMPegProcess != null)
				newFFMPegProcess.destroy();
		}

		
	}
}
