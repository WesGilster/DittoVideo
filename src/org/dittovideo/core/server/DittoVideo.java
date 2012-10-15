package org.dittovideo.core.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.dittovideo.core.httpserver.DirectStreamingServlet;
import org.dittovideo.core.httpserver.WebPresentationServlet;
import org.dittovideo.objectprovider.browser.FileSystemBrowserDIDLObjectProvider;
import org.dittovideo.objectprovider.testcontainer.TestingDIDLObjectProvider;
import org.dittovideo.objectprovider.wmc.WindowsMediaCenterDIDLObjectProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cfs.progress.LoggerProgressMonitor;
import com.cfs.progress.ProgressableObjectMonitor;
import com.sun.jna.Platform;

public class DittoVideo implements Runnable {
	public static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(15);
	private static final Logger LOGGER = LoggerFactory.getLogger(DittoVideo.class);
	
	private static MediaServerSetup mediaServerProperties = new MediaServerSetup();
	private static DittoVideo dittoServer;
	private LocalDevice mediaServer;
	private ControlPoint controlPoint;
	
	public static class MediaServerSetup {
		//TODO: Why are these public, BAD BAD BAD!!!
		public String deviceName = "Ditto Video";
		public String manufacturer = "Anonymous";
		public int httpPort = 8999;
		public Integer upnpPort = 5001;//leave null to pick a random port, but doing so breaks the cache on Sony Bravia...
		public String directStreamingContext = "direct";
		public String directStreamingThumbnailContext = "icon";
		public String webPresentationContext = "home";
		public String webPresentationPage = "index.html";
		public DirectStreamingServlet directStreamingServlet = new DirectStreamingServlet();
		public ProgressableObjectMonitor progressable = new LoggerProgressMonitor(true);
		public WebPresentationServlet webPresentationServlet = new WebPresentationServlet();
		
		private List<DIDLObjectProvider> providers = null;
		private CountDownLatch mediaServerInitialized = new CountDownLatch(1);
		private String boundAddress;
		
		public List<DIDLObjectProvider> getProviders() throws InterruptedException {
				mediaServerInitialized.await();
				
				if (mediaServerProperties.providers == null) {
					mediaServerProperties.providers = new ArrayList<DIDLObjectProvider>();
					if (Platform.isWindows()) {
						mediaServerProperties.providers.add(new WindowsMediaCenterDIDLObjectProvider());
					}
					mediaServerProperties.providers.add(new TestingDIDLObjectProvider());
					mediaServerProperties.providers.add(new FileSystemBrowserDIDLObjectProvider());
					//TODO: Eric's channel builder via script  (js, LUA)
					//TODO: ControlPanel();
				}
				
				return providers;
		}
		
		public String getBoundAddress() throws InterruptedException {
			mediaServerInitialized.await();
			return boundAddress;
		}
		
		public void setBoundAddress(String boundAddress) {
			mediaServerInitialized.countDown();
			this.boundAddress = boundAddress;
		}		
	}
	
	public DittoVideo(int deviceVersion) throws UnknownHostException, URISyntaxException, ValidationException, InterruptedException {
		UDN udn = UDN.uniqueSystemIdentifier(mediaServerProperties.deviceName + deviceVersion + mediaServerProperties.manufacturer);
		DeviceType type = new UDADeviceType("MediaServer", deviceVersion);
		DeviceDetails details = new DeviceDetails(mediaServerProperties.deviceName,
				new ManufacturerDetails(mediaServerProperties.manufacturer), 
					new ModelDetails(
							mediaServerProperties.deviceName, 
							mediaServerProperties.deviceName, 
						"v" + deviceVersion), 
				new URI("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + getSetup().httpPort + "/" + getSetup().webPresentationContext + "/" + getSetup().webPresentationPage), 
				new DLNADoc[] {
						new DLNADoc("DMS", DLNADoc.Version.V1_5),
						new DLNADoc("M-DMS", DLNADoc.Version.V1_5) },
				null);

		LocalService contentManagerService = new AnnotationLocalServiceBinder().read(CustomContentDirectoryService.class);
		contentManagerService.setManager(new DefaultServiceManager<CustomContentDirectoryService>(contentManagerService, CustomContentDirectoryService.class));
		
		LocalService connectionManagerService = new AnnotationLocalServiceBinder().read(CustomConnectionManagerService.class);
		connectionManagerService.setManager(new DefaultServiceManager<CustomConnectionManagerService>(connectionManagerService, CustomConnectionManagerService.class));
		
		mediaServer = new LocalDevice(new DeviceIdentity(udn), type, details, new LocalService[]{contentManagerService, connectionManagerService});

		LOGGER.info("friendly name: " + details.getFriendlyName());
		LOGGER.info("manufacturer: " + details.getManufacturerDetails().getManufacturer());
		LOGGER.info("model: " + details.getModelDetails().getModelName());
	}
	
	@Override
	public void run() {
		try {
			final UpnpService upnpService = new UpnpServiceImpl(new CustomUpnpServiceConfiguration());

			// Broadcast a search message for all devices
			controlPoint = upnpService.getControlPoint();
			controlPoint.search(new STAllHeader());

			// Add the bound local device to the registry
			upnpService.getRegistry().addDevice(mediaServer);

			NetworkAddress hooked = upnpService.getRouter().getActiveStreamServers(upnpService.getRouter().getNetworkAddressFactory().getBindAddresses()[0]).get(0);
			mediaServerProperties.setBoundAddress(hooked.getAddress().toString());
			String httpAddress = "http:/" + hooked.getAddress()+ ":" + hooked.getPort();
			for (Resource device : upnpService.getRegistry().getResources()) {
				LOGGER.info(httpAddress + device.getPathQuery());
			}
			
			final Server jettyServer = new Server(new InetSocketAddress(hooked.getAddress(), mediaServerProperties.httpPort));
	        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	        jettyServer.setHandler(context);
	 
	        context.addServlet(new ServletHolder(mediaServerProperties.directStreamingServlet), "/" + mediaServerProperties.directStreamingContext + "/*");
	        context.addServlet(new ServletHolder(mediaServerProperties.webPresentationServlet), "/" + mediaServerProperties.webPresentationContext + "/*");

			jettyServer.start();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					upnpService.shutdown();
					jettyServer.destroy();
				}
			});
		} catch (Exception ex) {
			LOGGER.error("Exception occured", ex);
			System.exit(1);
		} finally {
			
		}
	}
	
	public LocalDevice getMediaServer() {
		return mediaServer;
	}

	public static MediaServerSetup getSetup() throws InterruptedException {
		return mediaServerProperties;
	}
	
	public static DittoVideo getDittoServer() {
		return dittoServer;
	}
	
	public ControlPoint getControlPoint() {
		return controlPoint;
	}

	public static void main(String[] args) throws Exception {
		//TODO: Read properties from disk here...
		dittoServer = new DittoVideo(1);
		EXECUTOR.submit(dittoServer);
	}
}
