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
import java.util.Iterator;

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
	private JsonObject availableAnswer = null; // might have several
	// set to true when the pengine is destroyed
	// note that we might still have answers held
	private boolean destroyed = false;
	private Query currentQuery = null;
	
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
			
			// TODO think this out away from computer
			this.currentQuery = po.getAskQuery();

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
				throw new CouldNotCreateException("bad response code (if 500, query was invalid? query threw Prolog exception?)" + Integer.toString(responseCode));
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
			
			// TODO handle answer here
			if(respObject.containsKey("answer")) {
				handleAnswer(respObject.getJsonObject("answer"));
			}
			
			JsonString eventjson = (JsonString)respObject.get("event");
			String evtstr = eventjson.getString();
			
			// TODO store slave_limit and have accessor
			if(evtstr.equals("destroy")) {
				this.destroyed = true;
			} else if(evtstr.equals("create")) {
				;
			} else {
				throw new CouldNotCreateException("create request event was" + evtstr + " must be create or destroy");
			}
			
			String id = ((JsonString)respObject.get("id")).getString();
			return id;
			
		} catch (IOException e) {
			throw new CouldNotCreateException(e.toString());
		} catch(SyntaxErrorException e) {
			throw new CouldNotCreateException(e.getMessage());
		}
	}

	/**
	 * @param jsonObject
	 * @throws SyntaxErrorException 
	 */
	private void handleAnswer(JsonObject answer) throws SyntaxErrorException {
		if(answer.containsKey("event")) {
			switch( ((JsonString)answer.get("event")).getString()) {
			case	"success":
				handleData(answer, true);
				break;
			case	"destroy":
				this.destroyed = true;
				handleData(answer, true);
				break;
			case	"failure":
				handleData(answer, false);
			default:
				throw new SyntaxErrorException("Bad event in answer" + ((JsonString)answer.get("event")).getString());
			}
		}
		/*
		 * 
		 * 		          "answer":{
		             "data":{
		                   "event":"failure",
		                   "id":"c7e9e0c5-84b6-4faa-bbcb-1e1139df1206",
		                   "time":0.000019857999999999985
		                   },
		             "event":"destroy",
		             "id":"c7e9e0c5-84b6-4faa-bbcb-1e1139df1206"
		         },
		 */
		
	}

	/**
	 * @param answer
	 * @param b
	 */
	private void handleData(JsonObject answer, boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void dumpStateDebug() {
		System.err.println(this.pengineID);
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public Iterator<Proof> ask(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}
}
