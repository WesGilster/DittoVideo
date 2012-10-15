package org.dittovideo.objectprovider.wmc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mediaserver.wmc.CategoryTreeNode;
import com.mediaserver.wmc.UPNPObject;
import com.mediaserver.wmc.WindowsMediaCenterFilter;
import com.mediaserver.wmc.WindowsMediaCenterInfo;

public class WindowsMediaCenterPlaylistUPNPObject extends PlaylistContainer implements UPNPObject<DIDLObject>  {
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsMediaCenterPlaylistUPNPObject.class);

	private WindowsMediaCenterFilter<DIDLObject> filter;
	private WindowsMediaCenterDIDLObjectProvider provider;
	private boolean childrenBuilt = false;
	
	public WindowsMediaCenterPlaylistUPNPObject(WindowsMediaCenterDIDLObjectProvider provider) {
		this.provider = provider;
	}

	@Override
	public void setNode(CategoryTreeNode node, List<WindowsMediaCenterInfo> potentials) {
		filter = new WindowsMediaCenterFilter<DIDLObject>(this, null, node, potentials);
		//TODO: publish elements to jetty to provider
		provider.cacheObject(this, null);
		setTitle(filter.getName());
	}

	@Override
	public void addVideoItem(WindowsMediaCenterInfo info) {
		//TODO: This should be a transcode stream not a direct stream.
		provider.addDirectStream(info.getThumbnailFile(), info.getThumbnailFile(), info.getTitle(), info.getStudio(), this);
	}

	@Override
	public void addContainer(String thumbnailName, CategoryTreeNode node, Collection<WindowsMediaCenterInfo> potentials) {
		WindowsMediaCenterPlaylistUPNPObject newFolder = new WindowsMediaCenterPlaylistUPNPObject(provider);
		filter = new WindowsMediaCenterFilter<DIDLObject>(newFolder, null, node, potentials);
		newFolder.setTitle(filter.getName());
		newFolder.setSearchable(true);
		newFolder.filter = filter;
		provider.cacheObject(newFolder, this);
	}
	
	private void buildCheck() {
		if (!childrenBuilt) {
			childrenBuilt = true;
			filter.buildChildren();
			
			setChildCount(getContainers().size() + getItems().size());
		}
	}
	
	public List<DIDLObject> getChildren() { 
		buildCheck();
		
		List<DIDLObject> children = new ArrayList<DIDLObject>();
		children.addAll(getContainers());
		children.addAll(getItems());
		return children;
	}

	@Override
	public void clearChildren() {
		containers.clear();
		items.clear();
	}

	@Override
	public Integer getChildCount() {
		buildCheck();

		return super.getChildCount();
	}

	@Override
	public List<Container> getContainers() {
		buildCheck();

		return super.getContainers();
	}

	@Override
	public List<Item> getItems() {
		buildCheck();

		return super.getItems();
	}
	
	public String toString() {
		return getTitle();
	}
}
