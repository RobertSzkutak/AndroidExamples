/*
    StoryActivity.java : Part of an example RSS Reader for Android
    Copyright (C) 2011  Robert L Szkutak II (http://robertszkutak.com)

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
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

public class StoryActivity extends Activity 
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsstory);
    
        Intent startingIntent = getIntent();
        
        TextView title = (TextView) findViewById(R.id.title);
        TextView story = (TextView) findViewById(R.id.story);
        TextView link = (TextView) findViewById(R.id.link);
        TextView pub = (TextView) findViewById(R.id.pub);
        
        if(startingIntent != null)
        {
        	title.setText(startingIntent.getStringExtra("title"));
        	story.setText(parseHTML("   " + startingIntent.getStringExtra("description")+"\n"));
        	link.setText("Read More:\n" + startingIntent.getStringExtra("link") + "\n\n");
        	pub.setText("Published on : " + startingIntent.getStringExtra("pubdate"));
        }    
        else
        	finish();
    }
	
	String parseHTML(String parse)
	{
		//Remove Tables
		while(parse.contains("<table"))
		{
			int tableStart = parse.indexOf("<table");
			int tableEnd = parse.indexOf("</table>", tableStart) + 8;
			String table = parse.substring(tableStart, tableEnd);
			parse = parse.replace(table, "\n   ");
		}
		//Remove Images
		while(parse.contains("<img"))
		{
			int imageStart = parse.indexOf("<img");
			int imageEnd = parse.indexOf("/>", imageStart) + 2;
			String image = parse.substring(imageStart, imageEnd);
			parse = parse.replace(image, "\n   ");
		}
		//Parse URLs
		while(parse.contains("<a href="))
		{
			int linkStart = parse.indexOf("<a href=") + 9;
			int linkEnd = parse.indexOf(">", linkStart);
			String link = parse.substring(linkStart, linkEnd-1) + " ";
			linkStart = parse.indexOf("<a");
			linkEnd = parse.indexOf("</a>") + 4;
			String oldlink = parse.substring(linkStart, linkEnd);
			parse = parse.replace(oldlink, link);
		}
		
		//Combinational Tags
		parse = parse.replaceAll("<div></div>", "");
		parse = parse.replaceAll("<span></span>", "");
		parse = parse.replaceAll("<div><span>", "\n   ");
		parse = parse.replaceAll("<span><div>", "\n   ");
		
		//Individual Tags
		parse = parse.replaceAll("<br />", "");// "\n" ?
		parse = parse.replaceAll("<br>", "");// "\n" ?
		parse = parse.replaceAll("<div>", "\n   ");
		parse = parse.replaceAll("</div>", "");
		parse = parse.replaceAll("<em>", "");
		parse = parse.replaceAll("</em>", "");
    	parse = parse.replaceAll("<p>", "\n   ");
    	parse = parse.replaceAll("</p>", "");
		parse = parse.replaceAll("<span>", "\n   ");
		parse = parse.replaceAll("</span>", "");
		
		//HTML Hex
    	parse = parse.replaceAll("&#160;", " ");
    	
		return parse;
	}
}
