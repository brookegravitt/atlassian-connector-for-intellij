package com.atlassian.theplugin.idea.crucible;

public class CruciblePatchUploader implements Runnable {
	private final String patch;

	public CruciblePatchUploader(String patch) {
		this.patch = patch;
	}

	public void run() {
		final CruciblePatchUploadForm patchUploadForm = new CruciblePatchUploadForm();
		patchUploadForm.setPatchPreview(patch);
		patchUploadForm.show();
	}
}
