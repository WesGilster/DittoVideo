package org.dittovideo.core.server;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamClientImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomUpnpServiceConfiguration.class);

    @Override
    public StreamClient createStreamClient() {
        return new StreamClientImpl(new StreamClientConfigurationImpl());
    }

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        try {
        	if (DittoVideo.getSetup().upnpPort == null) {
	        	return new StreamServerImpl(
		    		new StreamServerConfigurationImpl(
		    				networkAddressFactory.getStreamListenPort()
		    				)
		    		);
        	} else {
        		return new StreamServerImpl(new StreamServerConfigurationImpl(DittoVideo.getSetup().upnpPort));
        	}
		} catch (InterruptedException e) {
			LOGGER.error("Couldn't create server because setup wasn't complete.");
			return null;
		}
    }

}