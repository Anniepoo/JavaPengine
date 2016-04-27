package com.simularity.os.javapengine;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
/*
 * Copyright (c) 2015 Simularity, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 * 
 */
import java.net.URL;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * This object is a reference to a remote pengine slave.
 * 
 * To make one use {@link PengineFactory}
 * 
 * @author anniepoo
 *
 */
public final class Pengine {
	// we copy the passed in object to make it immutable
	private final PengineBuilder po;
	@SuppressWarnings("unused")
	private final String pengineID;
	
	/**
	 * Create a new pengine object from a set of {@link PengineBuilder}.
	 * The {@link PengineBuilder} are cloned internally, so the passed 
	 * PengineOptions can be modified after this call
	 * 
	 * @param poo the PengineOptions to pass
	 * @throws CouldNotCreateException  if for any reason the pengine cannot be created
	 */
	Pengine(final PengineBuilder poo) throws CouldNotCreateException {
		try {
			this.po = (PengineBuilder) poo.clone();
		} catch (CloneNotSupportedException e) {
			throw new CouldNotCreateException("PengineOptions must be clonable");
		}
		
		pengineID = create(po);
	}

	/**
	 * does the actual creation, as a famulus of the constructor.
	 * 
	 * @param po the cloned PengineOptions
	 * @return the ID of the created pengine
	 * 
	 * @throws CouldNotCreateException if we can't make the pengine
	 */
	private String create(PengineBuilder po) throws CouldNotCreateException {
		URL url = po.getActualURL("create");
		StringBuffer response;

		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// above should get us an HttpsURLConnection if it's https://...

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "JavaPengine");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-type", "application/json");

			String urlParameters = po.getRequestBodyCreate();

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			try {
				wr.writeBytes(urlParameters);
				wr.flush();
			} catch (IOException e) {
				throw new CouldNotCreateException(e.getMessage());
			} finally {
				try {
					wr.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new CouldNotCreateException(e.toString());
				}
			}

			int responseCode = con.getResponseCode();
			if(responseCode < 200 || responseCode > 299) {
				throw new CouldNotCreateException("bad response code" + Integer.toString(responseCode));
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			try {
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			} catch (IOException e) {
				throw new CouldNotCreateException(e.toString());
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new CouldNotCreateException(e.toString());
				}
			}
			
			JsonReaderFactory jrf = Json.createReaderFactory(null);
			JsonReader jr = jrf.createReader(new StringReader(response.toString()));
			JsonObject respObject = jr.readObject();
			
			JsonString eventjson = (JsonString)respObject.get("event");
			String evtstr = eventjson.getString();
			
			if(!evtstr.equals("create")) {
				throw new CouldNotCreateException("create request returned an event other than create");
			}
			
			String id = ((JsonString)respObject.get("id")).getString();
			return id;
			
		} catch (IOException e) {
			throw new CouldNotCreateException(e.toString());
		}
	}

	/**
	 * 
	 */
	public void dumpStateDebug() {
		System.err.println(this.pengineID);
	}
}
/*
 * This project has involved a lot of experimenting, as hte protocol is poorly documented. As such,
 * I'm retaining my notes here
 */
// <domain>/<path>/pengine/create
// application/json
// see server_url/4, line 1529 of pengines.pl
/*
* HTTP/1.1 200 OK
Server: nginx/1.4.6 (Ubuntu)
Date: Wed, 20 Apr 2016 20:53:39 GMT
Content-Type: text/x-prolog; charset=UTF-8
Content-Length: 65
Connection: close

http://www.oracle.com/technetwork/articles/java/json-1973242.html

Asks response looks like
success('8eb2ec31-fd63-43a2-a80b-4552b6d505d7',
	[q(b)],
	1.1307000000002065e-5,
	true)
	
	the last item is whether there are more
	
then when you get the last one, and it does a destroy (I guess?)

destroy('8eb2ec31-fd63-43a2-a80b-4552b6d505d7',
	success('8eb2ec31-fd63-43a2-a80b-4552b6d505d7',
		[q(c)],
		2.1917999999999244e-5,
		false))
		
		
	from pengines.pl http_pengine_create/1
	
	%   HTTP POST handler  for  =/pengine/create=.   This  API  accepts  the
%   pengine  creation  parameters  both  as  =application/json=  and  as
%   =www-form-encoded=.

Looks like event_to_json we want the lang to be json-s maybe? certainly need to look into it.
For the moment, just json




*/