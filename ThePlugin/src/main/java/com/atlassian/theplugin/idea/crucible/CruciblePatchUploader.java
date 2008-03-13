package com.atlassian.theplugin.idea.crucible;

public class CruciblePatchUploader implements Runnable {
	private final String patch;
	private final String commitMessage;

	public CruciblePatchUploader(String commitMessage, String patch) {
		this.commitMessage = commitMessage;
		this.patch = patch;
	}

	public void run() {
		final CruciblePatchUploadForm patchUploadForm = new CruciblePatchUploadForm(commitMessage);
		patchUploadForm.setPatchPreview(patch);
		patchUploadForm.setTitle("Create Patch Review");
		patchUploadForm.show();
	}
}
