package org.dittovideo.core.server;

import java.util.List;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

import com.cfs.progress.ProgressableObjectMonitor;
public interface DIDLObjectProvider {
	public List<DIDLObject> buildDIDLObjects(String uuid, ProgressableObjectMonitor monitor);
	public List<DIDLObject> getCachedDIDLObjects();
	public void refreshContainer(Container container);
	public DIDLObject getDIDLObject(String objectId);
}
