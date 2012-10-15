package org.dittovideo.objectprovider.browser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.Item;

public class VirtualFolder extends PlaylistContainer {
	private FileSystemBrowserDIDLObjectProvider provider;
	private List<FolderType> types = Arrays.asList(FolderType.Image, FolderType.Music, FolderType.Video);
	private File[] childFiles = null;
	private boolean hasAddedChildren = false;
	
	public enum FileType {
		MP3("(?i).*\\.mp3", true),
		
		MPEG("(?i).*\\.mpeg", true),
		MPG("(?i).*\\.mpg", true),
		AVI("(?i).*\\.avi", true),
		QUICK_TIME("(?i).*\\.mov", true),
		MV4("(?i).*\\.mv4", true),
		RIPPED_DVD("(?i).*\\\\VIDEO_TS", false),
		
		GIF("(?i).*\\.gif", true),
		JPG("(?i).*\\.jpg", true),
		JPEG("(?i).*\\.jpeg", true),
		PNG("(?i).*\\.png", true);
		
		private String regex;
		private boolean directStream;
		
		FileType(String regex, boolean directStream) {
			this.regex = regex;
			this.directStream = directStream;
		}

		public String getRegex() {
			return regex;
		}

		public boolean isDirectStream() {
			return directStream;
		}
	}
	
	public enum FolderType {
		Music{
			@Override
			public FileType[] getFileTypes() {
				return new FileType[] {FileType.MP3};
			}
		},
		Video{
			@Override
			public FileType[] getFileTypes() {
				return new FileType[] {FileType.MPEG, FileType.MPG, FileType.AVI, FileType.QUICK_TIME, FileType.MV4, FileType.RIPPED_DVD};
			}
		},
		Image {
			@Override
			public FileType[] getFileTypes() {
				return new FileType[] {FileType.GIF, FileType.JPG, FileType.JPEG, FileType.PNG};
			}
		};
		public abstract FileType[] getFileTypes();
	}
	
	public VirtualFolder(FileSystemBrowserDIDLObjectProvider provider) {
		this.provider = provider;
		
		childFiles = File.listRoots();
	}
	
	private VirtualFolder(FileSystemBrowserDIDLObjectProvider provider, File directory, List<FolderType> types) {
		this.provider = provider;
		this.types = types;
		
		childFiles = directory.listFiles();
	}
	
	@Override
	public List<Container> getContainers() {
		if (!hasAddedChildren) {
			addChildren(childFiles);
		}
		
		return super.getContainers();
	}

	@Override
	public List<Item> getItems() {
		if (!hasAddedChildren) {
			addChildren(childFiles);
		}
		
		return super.getItems();
	}

	private void addChildren(File[] directories) {
		hasAddedChildren = true;
		for (File currentFile : directories) {
			if (currentFile.isFile()) {//Must use isFile because isDirectory doesn't work for root level paths in windows
				for (FolderType type : types) {
					for (FileType fileType : type.getFileTypes()) {
						if (currentFile.getName().matches(fileType.getRegex())) {
							if (fileType.isDirectStream()) {
								provider.addDirectStream(currentFile, null, currentFile.getName(), null, this);
							} else {
								//TODO: addTranscodeStreamVideoItem();
							}
						}
					}
				}
			} else {
				VirtualFolder folder = new VirtualFolder(provider, currentFile, types);
				if (!currentFile.getName().equals("")) {
					folder.setTitle(currentFile.getName());
				} else {
					folder.setTitle(currentFile.getPath());
				}
				folder.setSearchable(true);
				
				provider.cacheObject(folder, this);
			}
		}
	}
}
