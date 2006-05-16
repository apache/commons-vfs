package org.apache.commons.vfs.operations.vcs;

import java.util.Calendar;

/**
 * todo: add class description here
 * 
 * @author Siarhei Baidun
 * @since 0.1
 */
public class VcsLogEntry
{
	/**
	 * 
	 */
	private String author;

	/**
	 * Revision.
	 */
	private long revision;

	/**
	 * Message.
	 */
	private String message;

	/**
	 * Date.
	 */
	private Calendar date;

	/**
	 * Path.
	 */
	private String path;

	/**
	 * 
	 * @param revision
	 * @param message
	 * @param date
	 * @param path
	 */
	public VcsLogEntry(final String author, final long revision,
			final String message, final Calendar date, final String path)
	{
		this.author = author;
		this.revision = revision;
		this.message = message;
		this.date = date;
		this.path = path;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * 
	 * @return
	 */
	public long getRevision()
	{
		return revision;
	}

	/**
	 * 
	 * @return
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * 
	 * @return
	 */
	public Calendar getDate()
	{
		return date;
	}

	/**
	 * 
	 * @return
	 */
	public String getPath()
	{
		return path;
	}
}
