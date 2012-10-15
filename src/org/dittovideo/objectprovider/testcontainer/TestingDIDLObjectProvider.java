package org.dittovideo.objectprovider.testcontainer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dittovideo.core.server.AbstractDIDLObjectProvider;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cfs.progress.ProgressableObjectMonitor;

public class TestingDIDLObjectProvider extends AbstractDIDLObjectProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestingDIDLObjectProvider.class);

	@Override
	public List<DIDLObject> buildDIDLObjects(ProgressableObjectMonitor monitor) {
		try {
			List<DIDLObject> cache = new ArrayList<DIDLObject>();
	
			// set up a container for each channel
			PlaylistContainer channelContainer = new PlaylistContainer();
			channelContainer.setTitle("My Test Channel");
			channelContainer.setCreator("Media Server");
			channelContainer.setSearchable(true);
			cacheObject(channelContainer, null);
	
			// content in channel container
			String name = "http://72.94.199.7/format/mpeg/sample/567fd6a0e0da4a8e81bdeb870de3b19c/DELTA.MPG";// Video file cannot be played
			// String name = "http://192.168.1.2:5001/get/0$2$8/DELTA.MPG";//ok
			name = "http://192.168.1.2:5001/get/0$2$10/trailer_iphone.m4v";//ok
			//name = "http://69.163.168.66/blender.org/peach/trailer/trailer_iphone.m4v";// File not found
			// MimeType mimeType = new MimeType("video", "quicktime");
			MimeType mimeType = new MimeType("video", "mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=11");
			URL url;
			url = new URL(name);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			Integer contentLength = connection.getContentLength();
			connection.disconnect();
			Res res = new Res(mimeType, contentLength.longValue(), name);
			res.setBitrate(0l);
			res.setNrAudioChannels(6l);
			res.setDuration("01:38:59.00");
			res.setResolution("720x480");
	
			VideoItem video = new VideoItem();
			video.setTitle("Delta");
			video.setCreator("Blender");
			video.setResources(Collections.singletonList(res));
			cacheObject(video, channelContainer);
	
			cache.add(channelContainer);
			return cache;
		} catch (Exception e) {
			LOGGER.error("Failed to build DIDLObject", e);
			return null;
		}

	}

	@Override
	public void refreshContainer(Container container) {
		// TODO: Not supported yet
	}
}
