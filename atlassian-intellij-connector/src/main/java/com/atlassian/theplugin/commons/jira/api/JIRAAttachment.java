package com.atlassian.theplugin.commons.jira.api;

import java.util.Calendar;

/**
 * User: kalamon
 * Date: Sep 2, 2009
 * Time: 4:31:44 PM
 */
public class JIRAAttachment {
    private final String id;
    private final String author;
    private final String filename;
    private final Long filesize;
    private final String mimetype;
    private final Calendar created;

    public JIRAAttachment(String id, String author, String filename, long filesize, String mimetype, Calendar created) {
        this.id = id;
        this.author = author;
        this.filename = filename;
        this.filesize = filesize;
        this.mimetype = mimetype;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getFilename() {
        return filename;
    }

    public Long getFilesize() {
        return filesize;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Calendar getCreated() {
        return created;
    }
}
