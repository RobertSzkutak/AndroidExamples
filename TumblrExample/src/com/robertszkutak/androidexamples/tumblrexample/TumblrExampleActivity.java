/*
TumblrExampleActivity.java
Copyright (C) 2011 : Robert L Szkutak II

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.robertszkutak.androidexamples.tumblrexample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class TumblrExampleActivity extends Activity 
{
	public static final String CONSUMER_KEY = "Get this from your Tumblr application settings page";
	public static final String CONSUMER_SECRET = "Get this from your Tumblr application settings page";
	
	public static final String ACCESS_TOKEN = "Optional : Get this from your Tumblr application settings page";
	public static final String ACCESS_TOKEN_SECRET = "Optional : Get this from your Tumblr application settings page";
	
	public static final String REQUEST_URL = "http://www.tumblr.com/oauth/request_token";
	public static final String ACCESS_URL = "http://www.tumblr.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://www.tumblr.com/oauth/authorize";

	public static final String	OAUTH_CALLBACK_SCHEME	= "oauthflow-tumblr";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	private TextView debugStatus;
	private EditText posttitle, poststring;
	private Button post, loginorout;
	
	private static Intent newIntent = null;
	
	private static SharedPreferences pref = null;
	
	private static String debug, token, secret, authURL, uripath;
	private static final boolean localauth = false;//Set to true if you give non-null values to ACCESS_TOKEN and ACCESS_TOKEN_SECRET and wish to authenticate without using the Android web browser
	
	private static CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    private static CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
    
    private static boolean auth = false, browser = false, browser2 = false;//These booleans determine which code is run every time onResume is executed.
    private static boolean loggedin = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{   
		super.onCreate(savedInstanceState);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
        if(localauth)
		{
		    token = ACCESS_TOKEN;
		    secret = ACCESS_TOKEN_SECRET;
		    auth = true;
		}
		else
		{	
			token = pref.getString("TUMBLR_OAUTH_TOKEN", "");
			secret = pref.getString("TUMBLR_OAUTH_TOKEN_SECRET", "");
			
			if(token != null && token != "" && secret != null && secret != "")
			{
				auth = true;
				loggedin = true;
			}
			else
				setAuthURL();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(localauth == false && auth == false)
		{	
			if(browser == true)
				browser2 = true;
			
			if(browser == false)
			{
				browser = true;
				newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authURL));
				startActivity(newIntent);
			}
			
			if(browser2 == true)
			{
				Uri uri = getIntent().getData();
				uripath = uri.toString();
			
				if (uri != null && uripath.startsWith(OAUTH_CALLBACK_URL))
				{
					String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
					try {

						provider.retrieveAccessToken(consumer, verifier);
				
						token = consumer.getToken();
						secret = consumer.getTokenSecret();
					
						final Editor editor = pref.edit();
						editor.putString("TUMBLR_OAUTH_TOKEN", token);
						editor.putString("TUMBLR_OAUTH_TOKEN_SECRET", secret);
						editor.commit();
						
						auth = true;
						loggedin = true;

					} catch (OAuthMessageSignerException e) 
					{
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) 
					{
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) 
					{
						e.printStackTrace();
					} catch (OAuthCommunicationException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		if(auth == true)
        {
	        setContentView(R.layout.main);

	        posttitle = (EditText)findViewById(R.id.posttitle);
	        poststring = (EditText)findViewById(R.id.post);
	        debugStatus = (TextView)findViewById(R.id.debug_status);
	        post = (Button) findViewById(R.id.btn_post);
	        loginorout = (Button) findViewById(R.id.loginorout);
			
        	debug = "Access Token: " + token + "\n\nAccess Token Secret: " + secret;
        	debugStatus.setText(debug);
        
        	post.setOnClickListener(new View.OnClickListener() 
        	{
            	public void onClick(View v)
            	{
            		if (isAuthenticated()) 
            		{
            			sendPost();
            		} else 
            		{
            			Toast toast = Toast.makeText(getApplicationContext(), "You are not logged into Tumblr", Toast.LENGTH_SHORT);
            			toast.show();
            		}
            	}
        	});
        	
        	loginorout.setOnClickListener(new View.OnClickListener() 
        	{
            	public void onClick(View v)
            	{
            		LogInOrOut();
            	}
        	});
        	
        	updateLoginStatus();
        }
		
		if(auth == false && browser2 == true)
			finish();
	}
	
	//Grabs the authorization URL from OAUTH and sets it to the String authURL member
	private void setAuthURL()
	{
		try 
		{
			if((token == null || token == "") && (secret == null || secret == "") && auth == false && browser == false)
				authURL = provider.retrieveRequestToken(consumer, OAUTH_CALLBACK_URL);
			
		} catch (OAuthMessageSignerException e) 
		{
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) 
		{
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) 
		{
			e.printStackTrace();
		} catch (OAuthCommunicationException e) 
		{
			e.printStackTrace();
		}
	}
	
	//Logs the user in or out of Tumblr depending on whether they are presently logged into Tumblr
	private void LogInOrOut()
	{
		if(isAuthenticated())
			logout();
		else
		{
			auth = browser = browser2 = false;
			setAuthURL();
			browser = true;
			newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authURL));
			startActivity(newIntent);
		}	
	}
	
	//Removes the shared preferences values and sets the current token and secret to null essentially logging the user out of Tumblr
	private void logout() 
	{
		final Editor edit = pref.edit();
		edit.remove("TUMBLR_OAUTH_TOKEN");
		edit.remove("TUMBLR_OAUTH_TOKEN_SECRET");
		edit.commit();
		
		token = null;
		secret = null;
		
		consumer = null;
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	    provider = null;
	    provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
	    
	    debug = "Access Token: " + token + "\n\nAccess Token Secret: " + secret;
    	debugStatus.setText(debug);
    	
    	loggedin = false;
		
		updateLoginStatus();
	}
	
	//Updates a TextView telling us whether or not we are logged into Tumblr
	private void updateLoginStatus()
	{
		if(isAuthenticated())
			loginorout.setText("Log out of Tumblr");
		else
			loginorout.setText("Log into Tumblr");
	}
	
	//Returns whether or not the user is logged into Tumblr successfully
	private boolean isAuthenticated()
	{
		//TODO : Write a real authentication check
		return loggedin;
	}
	
	//Sends a Post to Tumblr
	public void sendPost() 
	{
		String title = posttitle.getText().toString();
		String body = poststring.getText().toString();
		
		//TODO : Finish this
	}
}