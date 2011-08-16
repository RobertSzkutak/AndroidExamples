/*
AndroidTwitterExample.java
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

package com.robertszkutak.androidexamples.twitterexample;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class AndroidTwitterExample extends Activity 
{
	public static final String CONSUMER_KEY = "Get this from your Twitter application settings page";
	public static final String CONSUMER_SECRET = "Get this from your Twitter application settings page";
	
	public static final String ACCESS_TOKEN = "Optional : Get this from your Twitter application settings page";
	public static final String ACCESS_TOKEN_SECRET = "Optional : Get this from your Twitter application settings page";
	
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

	public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow-twitter";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	private TextView loginStatus, debugStatus;
	private EditText tweetstring;
	private Button tweet, loginout;
	
	private static Intent newIntent = null;//Used to launch the Android web browser which is used to sign the user into Twitter and return a token if successful
	
	private static SharedPreferences pref = null;
	
	private static String debug, token, secret, authURL, uripath;
	private static final boolean localauth = false;//Set to true if you give non-null values to ACCESS_TOKEN and ACCESS_TOKEN_SECRET and wish to authenticate without using the Android web browser
	
	private static CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    private static CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
    
    private static boolean auth = false, browser = false, browser2 = false;//These booleans determine which code is run every time onResume is executed.
	
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
			token = pref.getString("TWITTER_OAUTH_TOKEN", "");
			secret = pref.getString("TWITTER_OAUTH_TOKEN_SECRET", "");
			
			if(token != null && token != "" && secret != null && secret != "")
				auth = true;
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
				Uri uri = newIntent.getData();
				uripath = uri.toString();
			
				if (uri != null && uripath.startsWith(AUTHORIZE_URL)) {
					String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
					try {

						provider.retrieveAccessToken(consumer, verifier);
				
						token = consumer.getToken();
						secret = consumer.getTokenSecret();
						
						final Editor editor = pref.edit();
						editor.putString("TWITTER_OAUTH_TOKEN", token);
						editor.putString("TWITTER_OAUTH_TOKEN_SECRET", secret);
						editor.commit();
						
						auth = true;

					} catch (OAuthMessageSignerException e) {
						e.printStackTrace();
					} catch (OAuthNotAuthorizedException e) {
						e.printStackTrace();
					} catch (OAuthExpectationFailedException e) {
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(auth == true)
        {
	        setContentView(R.layout.main);

	        loginStatus = (TextView)findViewById(R.id.login_status);
	        tweetstring = (EditText)findViewById(R.id.tweet);
	        debugStatus = (TextView)findViewById(R.id.debug_status);
	        tweet = (Button) findViewById(R.id.btn_tweet);
	        loginout = (Button) findViewById(R.id.loginout);
			
        	debug = "Access Token: " + token + "\n\nAccess Token Secret: " + secret;
        	debugStatus.setText(debug);
        
        	tweet.setOnClickListener(new View.OnClickListener() 
        	{
            	public void onClick(View v)
            	{
            		sendTweet();
            	}
        	});
        	
        	loginout.setOnClickListener(new View.OnClickListener()
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
	public void setAuthURL()
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
	
	//Logs the user in or out of Twitter depending on whether they are presently logged into Twitter
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
	
	//Removes the shared preferences values and sets the current token and secret to null essentially logging the user out of Twitter
	private void logout() 
	{
		final Editor edit = pref.edit();
		edit.remove("TWITTER_OAUTH_TOKEN");
		edit.remove("TWITTER_OAUTH_TOKEN_SECRET");
		edit.commit();
		
		token = null;
		secret = null;
		
		consumer = null;
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	    provider = null;
	    provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
		
		updateLoginStatus();
	}
	
	//Updates a TextView telling us whether or not we are logged into Twitter
	public void updateLoginStatus() 
	{
		boolean ret = isAuthenticated();
		
		loginStatus.setText("Logged into Twitter : " + ret);
		
		if(ret == true)
			loginout.setText("Log out of Twitter");
		else
			loginout.setText("Log into Twitter");
	}

	//Returns the String in an EditText. If the String is over 140 characters it is shortened to 140 characters before being returned
	private String getTweetMessage() 
	{
		String theTweet = tweetstring.getText().toString();
		if(theTweet.length() > 140)
			theTweet = theTweet.substring(0, 140);
		return theTweet;
	}	
	
	//Sends a Tweet without disrupting the UI
	public void sendTweet() 
	{
		if(isAuthenticated())
		{
			Thread t = new Thread() 
			{
				public void run() 
				{
					try 
					{
						sendTweetToTwitter(getTweetMessage());
					} catch (Exception ex) 
					{
						ex.printStackTrace();
					}
				}

			};
			t.start();
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "You are not logged into Twitter", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
    
	//Checks to see if we are logged into Twitter with the correct tokens
	public boolean isAuthenticated() 
	{	
		if(token == null || secret == null || token == "" || secret == "")
		{
			debug = "";
			debugStatus.setText(debug);
			return false;
		}
		else
		{
			AccessToken a = new AccessToken(token,secret);
			Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			twitter.setOAuthAccessToken(a);
		
			try 
			{
				twitter.getAccountSettings();
				return true;
			} catch (TwitterException e) 
			{
				debug += "\n\n" + e.getMessage();
				debugStatus.setText(debug);
				return false;
			}
		}
	}
	
	//Uses Twitter4J to send a String to Twitter which is used as a Tweet
	public void sendTweetToTwitter(String msg) throws Exception 
	{   
		AccessToken a = new AccessToken(token,secret);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
        twitter.updateStatus(msg);
	}	
}