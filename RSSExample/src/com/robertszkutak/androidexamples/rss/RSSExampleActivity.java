/*
    RSSExampleActivity.java : Part of an example RSS Reader for Android
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RSSExampleActivity extends Activity implements OnItemClickListener 
{	
	public final String tag = "RSSReader";
	
	private RSSFeed feed = null;
	ProgressDialog dialog;
	AlertDialog alert;
	DownloadFeedTask task;
	SharedPreferences pref;
	boolean in;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        in = pref.getBoolean("native", true);
        new DownloadFeedTask().execute(new String (getFeed()));
    }
	
	public void onItemClick(AdapterView parent, View v, int position, long id)
	{
		Intent itemintent = null;
		
		if(in)
		{
			itemintent = new Intent(this, StoryActivity.class);
			itemintent.putExtra("title", feed.getItem(position).getTitle());
			itemintent.putExtra("description", feed.getItem(position).getDescription());
			itemintent.putExtra("link", feed.getItem(position).getLink());
			itemintent.putExtra("pubdate", feed.getItem(position).getPubDate());
		}
		else
			itemintent = new Intent(Intent.ACTION_VIEW, Uri.parse(feed.getItem(position).getLink().toString()));
		
	    startActivity(itemintent);
	}
    
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) 
    {  
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.newsoptions, menu);
        return true;
    }  
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {	
    	switch (item.getItemId()) 
    	{
    		case R.id.newssettings:
    			final CharSequence[] items = {"Latest Articles", 
    									      "Academic News", 
    				                          "Arts and Entertainment"};

    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setTitle("Select a feed");
    			builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int item) {
    					Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
    					final Editor editor = pref.edit();
    					switch(item)
    					{
    					case 0:
    						editor.putString("EXFEED", "http://ww2.fredonia.edu/News/DesktopModules/DnnForge%20-%20NewsArticles/Rss.aspx?TabID=1101&ModuleID=1878&MaxCount=25");
    						break;
    					case 1:
    						editor.putString("EXFEED", "http://ww2.fredonia.edu/News/DesktopModules/DnnForge%20-%20NewsArticles/RSS.aspx?TabID=1101&ModuleID=1878&CategoryID=14");
    						break;
    					case 2:
    						editor.putString("EXFEED", "http://ww2.fredonia.edu/News/DesktopModules/DnnForge%20-%20NewsArticles/RSS.aspx?TabID=1101&ModuleID=1878&CategoryID=28");
    						break;
    					}
    					editor.commit();
    					alert.cancel();
    					refreshCurrentFeed();
    				}
    			});
    			alert = builder.create();
    			alert.show();
    			break;
        	case R.id.newsrefresh:
        		refreshCurrentFeed();
        		break;
    	}	
    	return true;
    }
    
    public String getFeed()
    {
    	String ret = null;
    	ret = pref.getString("EXFEED", "http://ww2.fredonia.edu/News/DesktopModules/DnnForge%20-%20NewsArticles/Rss.aspx?TabID=1101&ModuleID=1878&MaxCount=25");
    	
    	return ret;
    }
	
	public void refreshCurrentFeed() {
		new DownloadFeedTask().execute(new String (getFeed()));
	}
	
	private class DownloadFeedTask extends AsyncTask<String, Void, RSSFeed> 
	{
		@Override
		protected RSSFeed doInBackground(String... RSSuri) 
		{
			try
			{
				// setup the url
				URL url = new URL(RSSuri[0]);
				// create the factory
				SAXParserFactory factory = SAXParserFactory.newInstance();
				// create a parser
				SAXParser parser = factory.newSAXParser();
				// create the reader (scanner)
				XMLReader xmlreader = parser.getXMLReader();
				// instantiate our handler
				RSSHandler theRssHandler = new RSSHandler();
				// assign our handler
				xmlreader.setContentHandler(theRssHandler);
				// get our data via the url class
				InputSource is = new InputSource(url.openStream());
				// perform the synchronous parse           
				xmlreader.parse(is);
				// get the results - should be a fully populated RSSFeed instance, or null on error
				return theRssHandler.getFeed();
			}
			catch (Exception ee)
			{
				return null;

			}
		}

		@Override
		protected void onPostExecute(RSSFeed _resultFeed) {
			feed = _resultFeed;
			dialog.cancel();
			UpdateDisplay();
		}
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(RSSExampleActivity.this, "Fetching feed...", "Downloading");

		}
	}
	    
	private void UpdateDisplay()
	{
		ListView itemlist = (ListView) findViewById(R.id.listView1);
	        
	    if (feed == null)
	    	return;

	    ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(this,android.R.layout.simple_list_item_1,feed.getAllItems());

	    itemlist.setAdapter(adapter);    
	    itemlist.setOnItemClickListener(this);    
	    itemlist.setSelection(0);
	     
	}	    
}