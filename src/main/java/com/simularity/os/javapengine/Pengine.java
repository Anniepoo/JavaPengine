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
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;

import com.simularity.os.javapengine.PengineState.PSt;

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
	private final String pengineID;
	
	private PengineState state = new PengineState();
	
	/**
	 * Pengines are created, used, and destroyed. 
	 * If the Pengine has been destroyed you can't use it any more.
	 * 
	 * @return true iff I've been destroyed
	 */
	public boolean isDestroyed() {
		return state.isIn(PengineState.PSt.DESTROYED);
	}

	/**
	 * If the Pengine is currently servicing the query, this will return the current Query.
	 * if not, it returns null.
	 * 
	 * Using the interactor metaphor, this returns null if it's at the ?- prompt, and the Query
	 * if it's at the 'blinking cursor waiting for ; or .' state
	 * 
	 * @return the current Query or null if there isn't one
	 * 
	 */
	public Query getCurrentQuery() {
		return currentQuery;
	}

	// the current query, or null
	private Query currentQuery = null;
	private int slave_limit = -1;
	
	/**
	 * Create a new pengine object from a {@link PengineBuilder}.
	 * 
	 * @param poo the PengineBuilder that's creating this Pengine
	 * 
	 * @throws CouldNotCreateException  if for any reason the pengine cannot be created
	 */
	Pengine(final PengineBuilder poo) throws CouldNotCreateException {
		try {
			this.po = (PengineBuilder) poo.clone();
		} catch (CloneNotSupportedException e) {
			state.destroy();
			throw new CouldNotCreateException("PengineBuilder must be clonable");
		}
		
		try {
			pengineID = create(po);
		} catch (PengineNotReadyException e) {
			state.destroy();
			throw new CouldNotCreateException("Pengine wasnt ready????");
		}
	}

	/**
	 * does the actual creation, as a famulus of the constructor.
	 * 
	 * @param po the cloned PengineOptions
	 * @return the ID of the created pengine
	 * 
	 * @throws CouldNotCreateException if we can't make the pengine
	 * @throws PengineNotReadyException 
	 */
	private String create(PengineBuilder po) throws CouldNotCreateException, PengineNotReadyException {
		state.must_be_in(PSt.NOT_CREATED);
		
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
			} finally {
				wr.close();
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
			} finally {
				in.close();
			}
			
			JsonReaderFactory jrf = Json.createReaderFactory(null);
			JsonReader jr = jrf.createReader(new StringReader(response.toString()));
			JsonObject respObject = jr.readObject();
			
			if(respObject.containsKey("slave_limit")) {
				this.slave_limit  = respObject.getJsonNumber("slave_limit").intValue();
			}
			
			JsonString eventjson = (JsonString)respObject.get("event");
			String evtstr = eventjson.getString();
			
			if(evtstr.equals("destroy")) {
				state.setState(PSt.DESTROYED);
			} else if(evtstr.equals("create")) {
				state.setState(PSt.IDLE);
			} else {
				throw new CouldNotCreateException("create request event was" + evtstr + " must be create or destroy");
			}
			
			if(po.getAsk() != null) {
				this.currentQuery = new Query(this, po.getAsk(), false);
				state.setState(PSt.ASK);
			}
			
			if(respObject.containsKey("answer")) {
				handleAnswer(respObject.getJsonObject("answer"));
			}

			String id = ((JsonString)respObject.get("id")).getString();
			if(id == null) 
				throw new CouldNotCreateException("no pengine id in create message");
			return id;
			
		} catch (IOException e) {
			state.destroy();
			throw new CouldNotCreateException(e.toString());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new CouldNotCreateException(e.getMessage());
		} 
	}

	/**
	 * @return the slave_limit
	 */
	public int getSlave_limit() {
		return slave_limit;
	}

	/**
	 * @param jsonObject
	 * @throws SyntaxErrorException 
	 */
	private void handleAnswer(JsonObject answer) throws SyntaxErrorException {
		try {
			if(answer.containsKey("event")) {
				switch( ((JsonString)answer.get("event")).getString()) {
				case	"success":
						handleData(answer, true);
					break;
				case	"destroy":
					handleData(answer, true);
					currentQuery.noMore();
					state.setState(PSt.DESTROYED);
					break;
				case	"failure":
					currentQuery.noMore();
					handleData(answer, false);
				default:
					throw new SyntaxErrorException("Bad event in answer" + ((JsonString)answer.get("event")).getString());
				}
			}
		} catch (PengineNotReadyException e) {
			throw new SyntaxErrorException(e.getMessage());
		}
	}
		/*
		 * 
		 * 		   "answer":{
		             "data":{
		                   "event":"failure",
		                   "id":"c7e9e0c5-84b6-4faa-bbcb-1e1139df1206",
		                   "time":0.000019857999999999985
		                   },
		             "event":"destroy",
		             "id":"c7e9e0c5-84b6-4faa-bbcb-1e1139df1206"
		         },
		 */

	/**
	 * @param answer
	 * @param validData whether the data within this answer is a new answer (eg it won't be if we failed)
	 * @throws PengineNotReadyException 
	 */
	private void handleData(JsonObject answer, boolean validData) throws PengineNotReadyException {
		if(validData && answer.containsKey("data") &&
				answer.getJsonObject("data").containsKey("event") &&
				answer.getJsonObject("data").getJsonString("event").equals("success"))
			this.currentQuery.addNewData(answer.getJsonArray("data"));
		
		if(answer.containsKey("more")) {
			boolean more = answer.getBoolean("more");
			
			if(!more) {
				state.setState(PSt.IDLE);
				this.currentQuery.noMore();
			}
		}
	}

	/**
	 * 
	 */
	public void dumpStateDebug() {
		System.err.println(this.pengineID);
	}

	/**
	 * @param query
	 * @param template
	 * @return
	 * @throws PengineNotReadyException 
	 */
	public Query ask(String query, String template) throws PengineNotReadyException {
		state.must_be_in(PSt.IDLE);
		
		if(this.currentQuery != null)
			throw new PengineNotReadyException("Have not extracted all answers from previous query (or stopped it)");
		
		this.currentQuery = new Query(this, query, template);
		
		return this.currentQuery;
	}


	/**
	 * @param query
	 * @return
	 * @throws PengineNotReadyException 
	 */
	public Query ask(String query) throws PengineNotReadyException {
		state.must_be_in(PSt.IDLE);
		
		if(this.currentQuery != null)
			throw new PengineNotReadyException("Have not extracted all answers from previous query (or stopped it)");
		
		this.currentQuery = new Query(this, query);
		
		return this.currentQuery;
	}
	
	/**
	 * @param ask 
	 * @param query 
	 * @throws CouldNotCreateException 
	 * 
	 */
	void doAsk(Query query, String ask) throws PengineNotReadyException {
		state.must_be_in(PSt.IDLE, PSt.ASK);
		
		URL url = po.getActualURL("ask");
		StringBuffer response;
// TODO can we abstract this?
		
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// above should get us an HttpsURLConnection if it's https://...

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "JavaPengine");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-type", "application/json");

			String urlParameters = po.getRequestBodyAsk(ask);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			try {
				wr.writeBytes(urlParameters);
				wr.flush();
			} finally {
				wr.close();
			}

			int responseCode = con.getResponseCode();
			if(responseCode < 200 || responseCode > 299) {
				throw new PengineNotAvailableException("bad response code (if 500, perhaps query was invalid? Or query threw Prolog exception?)" + Integer.toString(responseCode));
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			try {
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			} finally {
				in.close();
			}
			
			JsonReaderFactory jrf = Json.createReaderFactory(null);
			JsonReader jr = jrf.createReader(new StringReader(response.toString()));
			JsonObject respObject = jr.readObject();
			
			JsonString eventjson = (JsonString)respObject.get("event");
			String evtstr = eventjson.getString();
			
			// TODO need to use this much of it to probe
			
			if(respObject.containsKey("answer")) {
				handleAnswer(respObject.getJsonObject("answer"));
			}
			
		} catch (IOException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.toString());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		}
		
	}

	/**
	 *  the query will not use the pengine again and has no more results to give
	 *  
	 *  
	 * @param query
	 */
	void iAmFinished(Query query) {
		if(query.equals(this.currentQuery))
			this.currentQuery = null;
		
		if(state.equals(PSt.ASK)) {
			try {
				state.setState(PSt.IDLE);
			} catch (PengineNotReadyException e) {
				System.err.println("Internal error in iAmFinished");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param query
	 * @throws PengineNotReadyException 
	 */
	public void doNext(Query query) throws PengineNotReadyException {
		state.must_be_in(PSt.ASK);
		try {
			URL url = po.getActualURL("next");
			StringBuffer response;
// TODO can we abstract this?
		
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// above should get us an HttpsURLConnection if it's https://...

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "JavaPengine");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-type", "application/json");

			String urlParameters = po.getRequestBodyNext();

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			try {
				wr.writeBytes(urlParameters);
				wr.flush();
			} finally {
				wr.close();
			}

			int responseCode = con.getResponseCode();
			if(responseCode < 200 || responseCode > 299) {
				throw new PengineNotAvailableException("bad response code (if 500, perhaps query was invalid? Or query threw Prolog exception?)" + Integer.toString(responseCode));
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			try {
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			} finally {
				in.close();
			}
			
			JsonReaderFactory jrf = Json.createReaderFactory(null);
			JsonReader jr = jrf.createReader(new StringReader(response.toString()));
			JsonObject respObject = jr.readObject();
			
			JsonString eventjson = (JsonString)respObject.get("event");
			String evtstr = eventjson.getString();
			
			// TODO need to use this much of it to probe
			
			if(respObject.containsKey("answer")) {
				handleAnswer(respObject.getJsonObject("answer"));
			}
			
		} catch (IOException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.toString());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		}
	}
}
