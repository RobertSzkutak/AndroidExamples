/*
    RSSHandler.java : Part of an example RSS Reader for Android
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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;

public class RSSHandler extends DefaultHandler 
{
	RSSFeed _feed;
	RSSItem _item;
	String _lastElementName = "";
	boolean bFoundChannel = false;
	final int RSS_TITLE = 1;
	final int RSS_LINK = 2;
	final int RSS_DESCRIPTION = 3;
	final int RSS_CATEGORY = 4;
	final int RSS_PUBDATE = 5;

	StringBuffer chars = new StringBuffer(); 

	int depth = 0;
	int currentstate = 0;
	/*
	 * Constructor 
	 */
	public RSSHandler()
	{
	}

	/*
	 * getFeed - this returns our feed when all of the parsing is complete
	 */
	public RSSFeed getFeed()
	{
		return _feed;
	}


	public void startDocument() throws SAXException
	{
		// initialize our RSSFeed object - this will hold our parsed contents
		_feed = new RSSFeed();
		// initialize the RSSItem object - we will use this as a crutch to grab the info from the channel
		// because the channel and items have very similar entries..
		_item = new RSSItem();

	}
	public void endDocument() throws SAXException
	{
	}
	public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException
	{
		
		chars = new StringBuffer();
		
		depth++;
		if (localName.equals("channel"))
		{
			currentstate = 0;
			return;
		}
		if (localName.equals("image"))
		{

			// record our feed data - we temporarily stored it in the item :)
			_feed.setTitle(_item.getTitle());
			_feed.setPubDate(_item.getPubDate());
		}
		if (localName.equals("item"))
		{
			// create a new item
			_item = new RSSItem();
			return;
		}
		if (localName.equals("title"))
		{

			currentstate = RSS_TITLE;
			return;
		}
		if (localName.equals("description"))
		{
			currentstate = RSS_DESCRIPTION;
			return;
		}
		if (localName.equals("link"))
		{
			currentstate = RSS_LINK;
			return;
		}
		if (localName.equals("category"))
		{
			currentstate = RSS_CATEGORY;
			return;
		}
		if (localName.equals("pubDate"))
		{
			currentstate = RSS_PUBDATE;
			return;
		}
		// if we don't explicitly handle the element, make sure we don't wind up erroneously 
		// storing a newline or other bogus data into one of our existing elements
		currentstate = 0;
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		depth--;
		if (localName.equals("item"))
		{
			// add our item to the list!
			_feed.addItem(_item);
			return;

		}
		switch (currentstate)
		{
		case RSS_TITLE:

			_item.setTitle(chars.toString());
			currentstate = 0;
			break;
		case RSS_LINK:
			_item.setLink(chars.toString());
			currentstate = 0;
			break;
		case RSS_DESCRIPTION:
			_item.setDescription(chars.toString());
			currentstate = 0;
			break;
		case RSS_CATEGORY:
			_item.setCategory(chars.toString());
			currentstate = 0;
			break;
		case RSS_PUBDATE:
			_item.setPubDate(chars.toString());
			currentstate = 0;
			break;
		default:
			return;
		}
	}

	public void characters(char ch[], int start, int length)
	{
//		String theString = new String(ch,start,length);
//		Log.i("RSSReader","characters[" + theString + "]");

		
		chars.append(new String(ch, start, length));
		


	}
}

