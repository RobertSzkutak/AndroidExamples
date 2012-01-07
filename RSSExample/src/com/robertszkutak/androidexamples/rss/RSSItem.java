/*
    RSSItem.java : Part of an example RSS Reader for Android
    Copyright (C) 2011  Robert L Szkutak II (http://robertszkutak.com)
    Copyright (C) 2011  Daniel S Lips (lipsapps on Android Market)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.robertszkutak.androidexamples.rss;

public class RSSItem 
{
	private String _title = null;
	private String _description = null;
	private String _link = null;
	private String _category = null;
	private String _pubdate = null;

	RSSItem()
	{
	}
	void setTitle(String title)
	{
		_title = title;
	}
	void setDescription(String description)
	{
		_description = description;
	}
	void setLink(String link)
	{
		_link = link;
	}
	void setCategory(String category)
	{
		_category = category;
	}
	void setPubDate(String pubdate)
	{
		_pubdate = pubdate;
	}
	public String getTitle()
	{
		return _title;
	}
	public String getDescription()
	{
		return _description;
	}
	public String getLink()
	{
		return _link;
	}
	public String getCategory()
	{
		return _category;
	}
	public String getPubDate()
	{
		return _pubdate;
	}
	public String toString()
	{
		if (_title.length() > 42)
		{
			return _title.substring(0, 42) + "...";
		}
		return _title;
	}
}

