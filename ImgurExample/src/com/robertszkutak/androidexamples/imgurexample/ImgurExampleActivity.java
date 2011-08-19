/*
ImgurExampleActivity.java
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

package com.robertszkutak.androidexamples.imgurexample;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ImgurExampleActivity extends Activity 
{
	public static final String CONSUMER_KEY = "Obtain this from Imgur";
	public static final String CONSUMER_SECRET = "Obtain this from Imgur";
	
	public static final String ACCESS_TOKEN = "Optional : Get this from your Imgur application settings page";
	public static final String ACCESS_TOKEN_SECRET = "Optional : Get this from your Imgur application settings page";
	
	public static final String REQUEST_URL = "http://api.imgur.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.imgur.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.imgur.com/oauth/authorize";

	public static final String	OAUTH_CALLBACK_SCHEME	= "oauthflow-imgur";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	private TextView debugStatus;
	private EditText path;
	private Button upload, loginorout;
	
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
			token = pref.getString("IMGUR_OAUTH_TOKEN", "");
			secret = pref.getString("IMGUR_OAUTH_TOKEN_SECRET", "");
			
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
						editor.putString("IMGUR_OAUTH_TOKEN", token);
						editor.putString("IMGUR_OAUTH_TOKEN_SECRET", secret);
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

	        path = (EditText) findViewById(R.id.imagepath);
	        debugStatus = (TextView)findViewById(R.id.debug_status);
	        upload = (Button) findViewById(R.id.upload);
	        loginorout = (Button) findViewById(R.id.loginout);
	        
        	debug = "Access Token: " + token + "\n\nAccess Token Secret: " + secret;
        	debugStatus.setText(debug);
        
        	upload.setOnClickListener(new View.OnClickListener() 
        	{
            	public void onClick(View v)
            	{
            		if (isAuthenticated()) 
            		{
            			uploadImage();
            		} else 
            		{
            			Toast toast = Toast.makeText(getApplicationContext(), "You are not logged into Imgur", Toast.LENGTH_SHORT);
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
			Log.d("imgur", e.toString());
		} catch (OAuthNotAuthorizedException e) 
		{
			Log.d("imgur", e.toString());
		} catch (OAuthExpectationFailedException e) 
		{
			Log.d("imgur", e.toString());
		} catch (OAuthCommunicationException e) 
		{
			Log.d("imgur", e.toString());
		}
	}
	
	//Logs the user in or out of Imgur depending on whether they are presently logged into Imgur
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
	
	//Removes the shared preferences values and sets the current token and secret to null essentially logging the user out of Imgur
	private void logout() 
	{
		final Editor edit = pref.edit();
		edit.remove("IMGUR_OAUTH_TOKEN");
		edit.remove("IMGUR_OAUTH_TOKEN_SECRET");
		edit.commit();
		
		token = null;
		secret = null;
		
		consumer = null;
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	    provider = null;
	    provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
	    
	    debug = "Access Token: " + token + "\n\nAccess Token Secret: " + secret + "\n\n";
    	debugStatus.setText(debug);
    	
    	loggedin = false;
		
		updateLoginStatus();
	}
	
	//Updates a TextView telling us whether or not we are logged into Imgur
	private void updateLoginStatus()
	{
		if(isAuthenticated())
			loginorout.setText("Log out of Imgur");
		else
			loginorout.setText("Log into Imgur");
	}
	
	//Returns whether or not the user is logged into Imgur successfully
	private boolean isAuthenticated()
	{
		//TODO : Write a real authentication check
		return loggedin;
	}
	
	//Uploads an image to Imgur
	public void uploadImage() 
	{
		String imagePath = path.getText().toString();
		
		FileInputStream in;
        BufferedInputStream buf;
        Bitmap bMap = null;
        try 
        {
       	    in = new FileInputStream(imagePath);
            buf = new BufferedInputStream(in);
            bMap = BitmapFactory.decodeStream(buf);
            if (in != null) 
            	in.close();
            if (buf != null) 
            	buf.close();
        } catch (Exception e) 
        {
            Log.e("Error reading file", e.toString());
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        //TODO : Write something to detect JPEG or PNG or GIF etc.
        //TODO : Figure out how to get higher quality JPEG compression.
        bMap.compress(CompressFormat.JPEG, 0, bos);
		
        String data = null;
        data = Base64.encodeToString(bos.toByteArray(), false);
		
		HttpPost hpost = new HttpPost("http://api.imgur.com/2/account/images");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("image", data));
		nameValuePairs.add(new BasicNameValuePair("type", "base64"));
		
		try 
		{
			hpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) 
		{
			debug += e.toString();
		}
		
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		consumer.setTokenWithSecret(token, secret);
		try 
		{
			consumer.sign(hpost);
		} catch (OAuthMessageSignerException e) 
		{
			debug += e.toString();
		} catch (OAuthExpectationFailedException e) 
		{
			debug += e.toString();
		} catch (OAuthCommunicationException e) 
		{
			debug += e.toString();
		}
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp = null;
		try 
		{
			resp = client.execute(hpost);
		} catch (ClientProtocolException e) 
		{
			debug += e.toString();
		} catch (IOException e) 
		{
			debug += e.toString();
		}
		
		String result = null;
		try {
			result = EntityUtils.toString(resp.getEntity());
		} catch (ParseException e) 
		{
			debug += e.toString();
		} catch (IOException e) 
		{
			debug += e.toString();
		}
		
		debug += result;
		debugStatus.setText(debug);
	}
}