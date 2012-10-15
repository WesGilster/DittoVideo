package org.dittovideo.objectprovider.browser;

import java.util.ArrayList;
import java.util.List;

import org.dittovideo.core.server.AbstractDIDLObjectProvider;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

import com.cfs.progress.ProgressableObjectMonitor;

public class FileSystemBrowserDIDLObjectProvider extends AbstractDIDLObjectProvider {
	private VirtualFolder root = new VirtualFolder(this);
	
	@Override
	public void refreshContainer(Container container) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DIDLObject> buildDIDLObjects(ProgressableObjectMonitor monitor) {
		List<DIDLObject> allChildren = new ArrayList<DIDLObject>();
		allChildren.addAll(root.getContainers());
		allChildren.addAll(root.getItems());
		return allChildren;
	}
}
