package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

public class CrucibleFileInfoManager {

	private Map<String, Map<PermId, List<CrucibleFileInfo>>> filesMap =
			new HashMap<String, Map<PermId, List<CrucibleFileInfo>>>();

	private CrucibleFileInfoManager() {
	}

	private static CrucibleFileInfoManager instance = new CrucibleFileInfoManager();


	public static CrucibleFileInfoManager getInstance() {
		return instance;
	}

	@NotNull
	public synchronized List<CrucibleFileInfo> getFiles(Review review) {

		Map<PermId, List<CrucibleFileInfo>> listMap = filesMap.get(review.getServerUrl());
		if (listMap == null) {
			listMap = new HashMap<PermId, List<CrucibleFileInfo>>();
			filesMap.put(review.getServerUrl(), listMap);
			listMap.put(review.getPermId(), new ArrayList<CrucibleFileInfo>());
		}

		List<CrucibleFileInfo> files = listMap.get(review.getPermId());
		return files != null ? files : new ArrayList<CrucibleFileInfo>();
	}

	public synchronized void setFiles(Review review, List<CrucibleFileInfo> files) {

		Map<PermId, List<CrucibleFileInfo>> f = filesMap.get(review.getServerUrl());
		if (f == null) {
			f = new HashMap<PermId, List<CrucibleFileInfo>>();
			filesMap.put(review.getServerUrl(), f);
		}

		f.put(review.getPermId(), files);
	}
}
