package org.dittovideo.core.server;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dittovideo.core.httpserver.DirectStreamingServlet;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cfs.progress.ProgressableObjectMonitor;

public abstract class AbstractDIDLObjectProvider implements DIDLObjectProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDIDLObjectProvider.class);
	private Hashtable<String, DIDLObject> objectCache = new Hashtable<String, DIDLObject>();
	protected String uuid;
	private AtomicInteger uniqueIndex = new AtomicInteger(-1);
	private DirectStreamingServlet directServer;
	
	private List<DIDLObject> cache;
	
	public static enum HostStreamType {
		Transcode,
		Direct
	}
	
	public AbstractDIDLObjectProvider() {
		try {
			this.directServer = DittoVideo.getSetup().directStreamingServlet;
		} catch (InterruptedException e) {
			LOGGER.error("Couldn't start provider due to startup sequence being interrupted.", e);
			throw new IllegalAccessError("Server was interrupted in startup");
		}
	}

	@Override
	public final List<DIDLObject> buildDIDLObjects(String uuid, ProgressableObjectMonitor monitor) {
		this.uuid = uuid;
		this.cache = buildDIDLObjects(monitor);
		return cache;
	}

	@Override
	public List<DIDLObject> getCachedDIDLObjects() {
		return cache;
	}

	@Override
	public DIDLObject getDIDLObject(String objectId) {
		return objectCache.get(objectId);
	}
	
	public String cacheObject(DIDLObject object, Container parent) {
		String index = uuid + uniqueIndex.incrementAndGet();
		object.setId(index);
		if (parent != null) {
			object.setParentID(parent.getId());
			if (parent.getChildCount() == null)//This ends up calling build objects
				parent.setChildCount(1);
			else
				parent.setChildCount(parent.getChildCount() + 1);
			
			if (object instanceof Container) {
				parent.addContainer((Container)object);
			} else {
				parent.addItem((Item)object);
			}
		} 
		
		objectCache.put(index, object);
		return index;
	}
	
	public void hostFile(String fileId, File hostFile, HostStreamType streamType) {
		switch (streamType) {
			case Direct:
				directServer.serveFile(fileId, hostFile);
				break;
			
			case Transcode:
		}
	}
	
	public void addDirectStreamByURL(URL fileToStream, File thumbnailFile, String title, String creator, PlaylistContainer parent) {
		
		//TODO: hook fileToStream to videoMime
		MimeType videoMime = new MimeType("video", "mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=11");
		//TODO: hook thumbnailFile to iconMime
		MimeType thumbnailMime = new MimeType("image", "jpeg:DLNA.ORG_PN=JPEG_TN");

		try {
			//TODO: hook fileToStream to Video or Image or Audio
			VideoItem video = new VideoItem();
			String videoId = cacheObject(video, parent);
			
			HttpURLConnection connection;
			Integer contentLength = new Integer(-1);
			try {//TODO: should use mencoder for this...
				connection = (HttpURLConnection) fileToStream.openConnection();
				contentLength = connection.getContentLength();
				connection.disconnect();
			} catch (IOException e) {
				LOGGER.error("Couldn't make connection to server for size of content", e);
			}
			Res videoResource = new Res(videoMime, new Long(contentLength), fileToStream.toString());
			//TODO: Add these with mencoder?
			//videoResource.setBitrate(0l);
			//videoResource.setNrAudioChannels(6l);
			//videoResource.setDuration("01:38:59.00");
			//videoResource.setResolution("720x480");

			if (thumbnailFile != null) {
				String thumbnailId = DittoVideo.getSetup().directStreamingThumbnailContext + "/" + videoId;
				String thumbnailName = "http:/" + DittoVideo.getSetup().getBoundAddress() + ":" + DittoVideo.getSetup().httpPort + "/" + DittoVideo.getSetup().directStreamingContext + "/" + thumbnailId;
				
				hostFile(thumbnailId, thumbnailFile, HostStreamType.Direct);
				
			    Res iconResource = new Res(thumbnailMime, null, thumbnailName);
				video.setResources(Arrays.asList(new Res[]{videoResource, iconResource}));
			} else {
				video.setResources(Arrays.asList(new Res[]{videoResource}));
			}
			
			video.setTitle(title);
			video.setCreator(creator);
		} catch (InterruptedException e) {
			LOGGER.error("System was shutdown before it was initialized", e);
		}
	}
	
	public void addDirectStream(File fileToStream, File thumbnailFile, String title, String creator, PlaylistContainer parent) {
		
		//TODO: hook fileToStream to videoMime
		MimeType videoMime = new MimeType("video", "mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=11");
		//TODO: hook thumbnailFile to iconMime
		MimeType thumbnailMime = new MimeType("image", "jpeg:DLNA.ORG_PN=JPEG_TN");

		try {
			//TODO: hook fileToStream to Video or Image or Audio
			VideoItem video = new VideoItem();
			String videoId = cacheObject(video, parent);
			
			String videoName = "http:/" + DittoVideo.getSetup().getBoundAddress() + ":" + DittoVideo.getSetup().httpPort + "/" + DittoVideo.getSetup().directStreamingContext + "/" + videoId;
			Res videoResource = new Res(videoMime, fileToStream.length(), videoName);
			//TODO: Add these with mencoder?
			//videoResource.setBitrate(0l);
			//videoResource.setNrAudioChannels(6l);
			//videoResource.setDuration("01:38:59.00");
			//videoResource.setResolution("720x480");

			if (thumbnailFile != null) {
				String thumbnailId = DittoVideo.getSetup().directStreamingThumbnailContext + "/" + videoId;
				String thumbnailName = "http:/" + DittoVideo.getSetup().getBoundAddress() + ":" + DittoVideo.getSetup().httpPort + "/" + DittoVideo.getSetup().directStreamingContext + "/" + thumbnailId;
				
				hostFile(videoId, fileToStream, HostStreamType.Direct);
				hostFile(thumbnailId, thumbnailFile, HostStreamType.Direct);
				
			    Res iconResource = new Res(thumbnailMime, null, thumbnailName);
				video.setResources(Arrays.asList(new Res[]{videoResource, iconResource}));
			} else {
				hostFile(videoId, fileToStream, HostStreamType.Direct);
				video.setResources(Arrays.asList(new Res[]{videoResource}));
			}
			
			video.setTitle(title);
			video.setCreator(creator);
		} catch (InterruptedException e) {
			LOGGER.error("System was shutdown before it was initialized", e);
		}
	}
	
	public abstract List<DIDLObject> buildDIDLObjects(ProgressableObjectMonitor monitor);
}
