package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.crucible.CrucibleServerFacade;

public class CruciblePatchUploader implements Runnable {
	private final String patch;
	private final String commitMessage;
	private final CrucibleServerFacade crucibleServerFacade;

	public CruciblePatchUploader(CrucibleServerFacade crucibleServerFacade, String commitMessage, String patch) {
		this.crucibleServerFacade = crucibleServerFacade;
		this.commitMessage = commitMessage;
		this.patch = patch;
	}

	public void run() {
		final CruciblePatchUploadForm patchUploadForm = new CruciblePatchUploadForm(crucibleServerFacade, commitMessage);
		patchUploadForm.setPatchPreview(patch);
		patchUploadForm.setTitle("Create Patch Review");
		patchUploadForm.show();
	}
}
