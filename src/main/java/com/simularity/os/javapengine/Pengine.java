/**
 * Copyright (c) 2016 Simularity Inc.
 * 

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
import com.simularity.os.javapengine.exception.CouldNotCreateException;
import com.simularity.os.javapengine.exception.PengineNotAvailableException;
import com.simularity.os.javapengine.exception.PengineNotReadyException;
import com.simularity.os.javapengine.exception.SyntaxErrorException;

/**
 * This object is a reference to a remote pengine slave.
 * 
 * To make one use {@link PengineBuilder}
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
	 * Low level famulus to abstract out some of the HTTP handling common to all requests
	 * 
	 * @param url   The actual url to httpRequest
	 * @param contentType  The value string of the Content-Type header
	 * @param body    the body of the POST request
	 * @return  the returned JSON object
	 * 
	 * @throws CouldNotCreateException
	 * @throws IOException 
	 */
	private JsonObject penginePost(
			URL url,
			String contentType,
			String body
			) throws IOException {
		StringBuffer response;

		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// above should get us an HttpsURLConnection if it's https://...

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "JavaPengine");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-type", contentType);

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			try {
				wr.writeBytes(body);
				wr.flush();
			} finally {
				wr.close();
			}

			int responseCode = con.getResponseCode();
			if(responseCode < 200 || responseCode > 299) {
				throw new IOException("bad response code (if 500, query was invalid? query threw Prolog exception?)" + Integer.toString(responseCode));
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
			
			return respObject;
		} catch (IOException e) {
			state.destroy();
			throw e;
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
		
		try{
			JsonObject respObject = penginePost(
					po.getActualURL("create"), 
					"application/json", 
					po.getRequestBodyCreate());
				
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
			throw new CouldNotCreateException(e.getMessage());
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
	 * handle the result of a send
	 * 
	 * @param jsonObject
	 * @throws SyntaxErrorException 
	 */
	private void handleAnswer(JsonObject answer) throws SyntaxErrorException {
		try {
			if(answer.containsKey("event")) {
				switch( ((JsonString)answer.get("event")).getString()) {
				case	"success":
					if(answer.containsKey("data")) {
						currentQuery.addNewData(answer.getJsonArray("data"));
					}
					if(answer.containsKey("more")) {
						if(!answer.getBoolean("more")) {
							currentQuery.noMore();
						}
					}
					break;
					
				case	"destroy":
					if(answer.containsKey("data")) {
						// if it contains a data key, then strangely, it's an 'answer' structure
						handleAnswer(answer.getJsonObject("data"));
					}
					if(currentQuery != null)
						currentQuery.noMore();
					state.setState(PSt.DESTROYED);
					break;
					
				case	"failure":
					currentQuery.noMore();
					break;
					
				case	"stop":
					currentQuery.noMore();
					break;
					
				case	"error":
					throw new SyntaxErrorException("Error - probably invalid Prolog query?");
					
				default:
					throw new SyntaxErrorException("Bad event in answer" + ((JsonString)answer.get("event")).getString());
				}
			}
		} catch (PengineNotReadyException e) {
			throw new SyntaxErrorException(e.getMessage());
		}
	}

	/**
	 * 
	 */
	public void dumpStateDebug() {
		System.err.println(this.pengineID);
		System.err.println("slave_limit " + this.slave_limit);
		if(this.currentQuery != null)
			this.currentQuery.dumpDebugState();
		this.po.dumpDebugState();
		this.state.dumpDebugState();
		
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
		state.must_be_in(PSt.IDLE);
		if(currentQuery == null)
			this.currentQuery = query;		
		else
			throw new PengineNotReadyException("You already have a query in process");
		
		state.setState(PSt.ASK);
		try {
			JsonObject answer =  penginePost(
					po.getActualURL("send", this.getID()),
					"application/x-prolog; charset=UTF-8",
					po.getRequestBodyAsk(this.getID(), ask));
			
			handleAnswer(answer);
		} catch (IOException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		}
		
	}

	/**
	 *  the query will not use the pengine again
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
	void doNext(Query query) throws PengineNotReadyException {
		state.must_be_in(PSt.ASK);
		if(!query.equals(currentQuery)) {
			throw new PengineNotReadyException("Cannot advance more than one query - finish one before starting next");
		}
		
		try {
			JsonObject respObject =  penginePost(
					po.getActualURL("send", this.getID()),
					"application/x-prolog; charset=UTF-8",
					po.getRequestBodyNext());
			
			handleAnswer(respObject);
		} catch (IOException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public String getID() {
		state.must_be_in(PSt.ASK, PSt.IDLE);
		return this.pengineID;
	}

	/**
	 * Destroy the pengine.
	 * this makes a best attempt to destroy the pengine.
	 * 
	 * after calling destroy you should not further release the pengine
	 */
	public void destroy() {
		if(state.isIn(PSt.DESTROYED))
			return;
		
		if(state.isIn(PSt.NOT_CREATED)) {
			state.destroy();
			return;
		}
			
		state.must_be_in(PSt.ASK, PSt.IDLE);
		
		try {
			JsonObject respObject =  penginePost(
					po.getActualURL("send", this.getID()),
					"application/x-prolog; charset=UTF-8",
					po.getRequestBodyDestroy());
			
			handleAnswer(respObject);
		} catch (IOException e) {
			e.printStackTrace();
			//throw new PengineNotAvailableException(e.getMessage());
		} catch(SyntaxErrorException e) {
			e.printStackTrace();
			//throw new PengineNotAvailableException(e.getMessage());
		} catch (PengineNotReadyException e) {
			e.printStackTrace();
		} finally {
			state.destroy();
		}
	}
	
	protected void finalize() {
		destroy();
	}

	/**
	 * @throws PengineNotReadyException 
	 * 
	 */
	void doStop() throws PengineNotReadyException {
		state.must_be_in(PSt.ASK);
		
		try {
			JsonObject respObject =  penginePost(
					po.getActualURL("send", this.getID()),
					"application/x-prolog; charset=UTF-8",
					po.getRequestBodyStop());
			
			handleAnswer(respObject); // we might destroy it
		} catch (IOException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		} catch(SyntaxErrorException e) {
			state.destroy();
			throw new PengineNotAvailableException(e.getMessage());
		}
	}
}
