package org.dittovideo.core.server;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomContentDirectoryService extends AbstractContentDirectoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomContentDirectoryService.class);

	private PlaylistContainer ROOT = null;
	private List<Future<List<DIDLObject>>> futures = null;
	private List<DIDLObjectProvider> providers = null;
	
	public CustomContentDirectoryService() {
		futures = new ArrayList<Future<List<DIDLObject>>>();
		try {
			providers = DittoVideo.getSetup().getProviders();
	        for (final DIDLObjectProvider currentProvider : providers) {
	        	futures.add(DittoVideo.EXECUTOR.submit(new Callable<List<DIDLObject>>() {
					@Override
					
					public List<DIDLObject> call() throws Exception {
			        	String uuid = UUID.randomUUID() + "$";
						return currentProvider.buildDIDLObjects(uuid, DittoVideo.getSetup().progressable);
					}
				}));
	        }
		} catch (InterruptedException e) {
			LOGGER.error("Startup interrupted", e);
		} catch (Exception e) {
			LOGGER.error("Providers couldn't be loaded", e);
		}
	}
	
    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                    String filter, long firstResult, long maxResults,
                    SortCriterion[] orderby) throws ContentDirectoryException {
    	LOGGER.info("---------------------");
    	LOGGER.info("objectID:" + objectID);
    	LOGGER.info("browseFlag:" + browseFlag);
    	LOGGER.info("filter:" + filter);
    	LOGGER.info("firstResult:" + firstResult);
    	LOGGER.info("maxResults:" + maxResults);
    	LOGGER.info("orderby:" + Arrays.toString(orderby));

    	//TODO: add filter for audio, pictures, video
    	if (ROOT == null) {
        	LOGGER.info("---------------------");
        	ROOT = new PlaylistContainer();
        	ROOT.setParentID("-1");//Do we need to do this?
        	ROOT.setTitle("MediaServer root");
        	ROOT.setCreator("Media Server");
        	ROOT.setSearchable(true);
        	ROOT.setChildCount(0);
        	ROOT.setId("0");
        	
        	for (Future<List<DIDLObject>> currentFuture : futures) {
        		try {
					List<DIDLObject> rootContainers = currentFuture.get();
					for (DIDLObject didlObject : rootContainers) {
						if (didlObject instanceof Container) {
							Container container = ((Container)didlObject);
							container.setParentID(ROOT.getId());
							ROOT.addContainer(container);
						} else {
							Item item = ((Item)didlObject);
							item.setParentID(ROOT.getId());
							ROOT.addItem(item);
						}
						LOGGER.info("added item:" + didlObject.getTitle());
						ROOT.setChildCount(ROOT.getChildCount() + 1);
					}
				} catch (Exception e) {
		        	LOGGER.error("Problem building cache (will try other providers)", e);
				}
        	}
    	}
    	
        try {
    		BrowseResult browseResult;
            DIDLContent didl = new DIDLContent();
            DIDLObjectProvider providerFound = null;
            DIDLObject contentNode = null;
            for (DIDLObjectProvider currentProvider : providers) {
            	contentNode = currentProvider.getDIDLObject(objectID);
            	if (contentNode != null) {
            		providerFound = currentProvider;
            		break;
            	}
            }
            
            if (contentNode == null) {
            	if (!objectID.equals("0")) {
            		LOGGER.info("Couldn't find objectID:" + objectID);
            		throw new IllegalArgumentException("Couldn't find objectID:" + objectID);
            	}
            	
            	contentNode = ROOT;
            }
            

            if(contentNode != null && contentNode instanceof Item) {
            	didl.addItem((Item) contentNode);
            	browseResult = new BrowseResult(new DIDLParser().generate(didl), 1, 1);
            } else if(contentNode != null && contentNode instanceof Container){
            	Container tmpContainer = (Container) contentNode;
            	for(Container container: tmpContainer.getContainers()) {
            		didl.addContainer(container);
            	}
            	
            	for(Item item: tmpContainer.getItems()) {
            		didl.addItem(item);
            	}
					/*String didlString = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" +
						"<item id=\"0$12$9$1\" parentID=\"0$12$9\" restricted=\"false\">" +
						"<dc:title>DELTA test</dc:title>" +
					    //"<res protocolInfo=\"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=11:*\" " +
					   "<res protocolInfo=\"http-get:*:video/mpeg:DLNA.ORG_OP=11;DLNA.ORG_PN=MPEG_PS_NTSC:*\"" +
					    //"<res protocolInfo=\"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=11:*\" " +
					    //"<res protocolInfo=\"http-get:*:video/mpeg:dlna.org_pn=MPEG_PS_NTSC;dlna.org_op=11:*\" " +
					    //"duration=\"01:38:59.00\" " +
					    //"resolution=\"720x480\" " +
					    //"bitrate=\"0\" " +
					    //"nrAudioChannels=\"6\"" +
					    ">http://www.fileformat.info/format/mpeg/sample/567fd6a0e0da4a8e81bdeb870de3b19c/DELTA.MPG</res>" + 
					    "<upnp:class>object.item.videoItem</upnp:class>" +
					    "</item>" +
					    "</DIDL-Lite>";//*/
					
					//browseResult = new BrowseResult(didlString, tmpContainer.getChildCount(), tmpContainer.getChildCount());
            		browseResult = new BrowseResult(new DIDLParser().generate(didl), tmpContainer.getChildCount(), tmpContainer.getChildCount());
            } else {
            	browseResult = new BrowseResult("", 0, 0);
            }

            LOGGER.info("ContainerUpdateID:" + browseResult.getContainerUpdateIDLong());
            LOGGER.info("Count:" + browseResult.getCountLong());
            LOGGER.info("Result:" + browseResult.getResult());
            LOGGER.info("TotalMatches:" + browseResult.getTotalMatchesLong());
            LOGGER.info("---------------------");
            return browseResult;
        } catch (Exception ex) {
        	LOGGER.error("Exception building DIDL", ex);
                throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
        }
    }

	@Override
	public BrowseResult search(String containerId, String searchCriteria,
			String filter, long firstResult, long maxResults,
			SortCriterion[] orderBy) throws ContentDirectoryException {
		// You can override this method to implement searching!
		return super.search(containerId, searchCriteria, filter, firstResult,
				maxResults, orderBy);
	}
}
