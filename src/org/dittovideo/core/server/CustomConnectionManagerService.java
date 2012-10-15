package org.dittovideo.core.server;

import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.connectionmanager.AbstractPeeringConnectionManagerService;
import org.fourthline.cling.support.model.ConnectionInfo;
import org.fourthline.cling.support.model.ConnectionInfo.Direction;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomConnectionManagerService extends AbstractPeeringConnectionManagerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomConnectionManagerService.class);
	
	public CustomConnectionManagerService() {
		super(new ProtocolInfos(
				"http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM," +
				"http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED," +
				"http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG," +
				"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3," +
				"http-get:*:audio/L16:DLNA.ORG_PN=LPCM," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=AVC_TS_HD_24_AC3_ISO;SONY.COM_PN=AVC_TS_HD_24_AC3_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_24_AC3;SONY.COM_PN=AVC_TS_HD_24_AC3," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_24_AC3_T;SONY.COM_PN=AVC_TS_HD_24_AC3_T," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_PS_PAL," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_PS_NTSC," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_50_L2_T," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_60_L2_T," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_SD_EU_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_EU," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_EU_T," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_50_AC3_T," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_HD_50_L2_ISO;SONY.COM_PN=HD2_50_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_SD_60_AC3_T," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_HD_60_L2_ISO;SONY.COM_PN=HD2_60_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_HD_50_L2_T;SONY.COM_PN=HD2_50_T," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_HD_60_L2_T;SONY.COM_PN=HD2_60_T," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=AVC_TS_HD_50_AC3_ISO;SONY.COM_PN=AVC_TS_HD_50_AC3_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_50_AC3;SONY.COM_PN=AVC_TS_HD_50_AC3," +
				"http-get:*:video/mpeg:DLNA.ORG_PN=AVC_TS_HD_60_AC3_ISO;SONY.COM_PN=AVC_TS_HD_60_AC3_ISO," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_60_AC3;SONY.COM_PN=AVC_TS_HD_60_AC3," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_50_AC3_T;SONY.COM_PN=AVC_TS_HD_50_AC3_T," +
				"http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_HD_60_AC3_T;SONY.COM_PN=AVC_TS_HD_60_AC3_T," +
				"http-get:*:video/x-mp2t-mphl-188:*," +
				"http-get:*:*:*," +
				"http-get:*:video/*:*," +
				"http-get:*:audio/*:*," +
				"http-get:*:image/*:*"), null, new ConnectionInfo());
	}
	
	@Override
	protected ConnectionInfo createConnection(int connectionID,
			int peerConnectionId, ServiceReference peerConnectionManager,
			Direction direction, ProtocolInfo protocolInfo)
			throws ActionException {
		LOGGER.info("Create Connection");
		return null;
	}

	@Override
	protected void closeConnection(ConnectionInfo connectionInfo) {
		// TODO Auto-generated method stub
		LOGGER.info("Asked to close Connection");
	}

	@Override
	protected void peerFailure(ActionInvocation invocation,
			UpnpResponse operation, String defaultFailureMessage) {
		LOGGER.info("Peer open failed");
	}
}
