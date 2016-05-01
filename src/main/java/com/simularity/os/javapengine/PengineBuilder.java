package com.simularity.os.javapengine;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

/*
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
public final class PengineBuilder implements Cloneable, PengineFactory {
	private URL server = null;
	private String application = "sandbox";
	private String ask = null;
	private int chunk = 1;
	private boolean destroy = true;
	private String srctext = null;
	private URL srcurl = null;
	private String format = "json";   // TODO make this an enum
	private String alias = null;
	
	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final PengineBuilder clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (PengineBuilder)super.clone();
	}

	/**
	 * Get the actual URL to request from
	 * 
	 * TODO change action from string to enum
	 * 
	 * @param action the action to request - create, ask, next, etc - as a string
	 * @return
	 * @throws CouldNotCreateException 
	 */
	URL getActualURL(String action) throws PengineNotReadyException {
		StringBuffer msg = new StringBuffer("none");
		
		if(server == null) {
			throw new PengineNotReadyException("Cannot get actual URL without setting server");
		}
		try {		
			URI uribase = server.toURI();
			if (uribase.isOpaque()) {
				throw new PengineNotReadyException("Cannot get actual URL without setting server");
			}
			
			URI relative = new URI("/pengine/" + action);
			
			URI fulluri = uribase.resolve(relative);
			msg.append(fulluri.toString());
			return fulluri.toURL();
		} catch (MalformedURLException e) {
			throw new PengineNotReadyException("Cannot form actual URL for action " + action + " from uri " + msg.toString());
		} catch (URISyntaxException e) {
			throw new PengineNotReadyException("URISyntaxException in getActualURL");
		}
	}

	/**
	 * @return a string representation of the request body for the create action
	 */
	String getRequestBodyCreate() {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder job = factory.createObjectBuilder();
		
		if(!this.destroy) {
			job.add("destroy", "false");
		}
		if(this.chunk > 1) {
			job.add("chunk", this.chunk);
		}
		job.add("format", this.format);

		if(this.srctext != null) {
			job.add("srctext", this.srctext);
		}
		if(this.srcurl != null) {
			job.add("srcurl", this.srcurl.toString());
		}
		
		// test protocol
	//	job.add("ask", "member((X,Y), [(a(taco),3),(b,4),(c,5)])");
		// job.add("template", "X");
		
		// this will be a json object with fields for options
		// sample, as a prolog dict
		//_{ src_text:"\n            q(X) :- p(X).\n            p(a). p(b). p(c).\n        "}
		return job.build().toString();
	}

	/**
	 * @param urlstring String that represents the server URL - this does not contain the /pengines/create extension
	 * @throws MalformedURLException if the string can't be turned into an URL
	 */
	public void setServer(String urlstring) throws MalformedURLException {
		server = new URL(urlstring);
	}

	/**
	 * @param server the server base URL - this does not contain the /pengines/create extension
	 */
	public void setServer(URL server) {
		this.server = server;
	}
	
	/**
	 * @return the server
	 */
	public URL getServer() {
		return server;
	}


	/**
	 * @return the application name
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * @return the query that will be sent along with the create, or null if none
	 */
	public String getAsk() {
		return ask;
	}

	/**
	 * @param ask the query to be sent along with the create, or null to not send one
	 */
	public void setAsk(String ask) {
		this.ask = ask;
	}

	/**
	 * @return the number of answers to return in one HTTP request
	 */
	public int getChunk() {
		return chunk;
	}

	/**
	 * @param chunk the max number of answers to return in one HTTP request - defaults to 1
	 */
	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	/**
	 * @return true if we will destroy the pengine at the close of the first query
	 */
	public boolean isDestroy() {
		return destroy;
	}

	/**
	 * @param destroy Destroy the pengine when the first query concludes?
	 */
	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	/**
	 * @return the srctext  @see setSrctext
	 */
	public String getSrctext() {
		return srctext;
	}

	/**
	 * @param srctext Additional Prolog code, which must be safe, to be included in the pengine's knowledgebase
	 */
	public void setSrctext(String srctext) {
		this.srctext = srctext;
	}

	/**
	 * @return the URL of some additional Prolog code, which must be safe, to be included in the pengine's knowledgebase
	 */
	public URL getSrcurl() {
		return srcurl;
	}

	/**
	 * @param srcurl the srcurl to set
	 */
	public void setSrcurl(URL srcurl) {
		this.srcurl = srcurl;
	}

	/**
	 * @return the format, one of "json" (default), "json-s", or "prolog"
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the response format, one of "json" (default), "json-s", or "prolog"
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the alias or null
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias a string name to refer to the pengine by (remove by passing this null)
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	// todo synchronize all this, so we can't change during the pengine create time
	
	/* ======================================   Implement PengineFactory =========================== */
	public Pengine newPengine() throws CouldNotCreateException {
		return new Pengine(this);
	}

	/**
	 * return the POST body for a /pengines/ask request of ask
	 * 
	 * @param id   The pengine id that is transmitting
	 * @param ask   The Prolog query
	 * @return   the body
	 */
	public String getRequestBodyAsk(String id, String ask) {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);  // TODO should this be static member?
		JsonObjectBuilder job = factory.createObjectBuilder();
		
		if(!this.destroy) {
			job.add("destroy", "false");
		}
		if(this.chunk > 1) {
			job.add("chunk", this.chunk);
		}
		job.add("format", this.format);

		job.add("ask", ask);
		job.add("id", id);
		
		// test protocol
	//	job.add("ask", "member((X,Y), [(a(taco),3),(b,4),(c,5)])");
		// job.add("template", "X");
		
		// this will be a json object with fields for options
		// sample, as a prolog dict
		//_{ src_text:"\n            q(X) :- p(X).\n            p(a). p(b). p(c).\n        "}
		return job.build().toString();
	}

	/**
	 * @return
	 */
	public String getRequestBodyNext() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* eventually we have this
	public Query newPengineOnce(String ask) {
		return newPengineOnce(this.po, ask);
	}
	
	public Query newPengineOnce(PengineOptions po, String ask) {
				
	}
	
	public Proof newPengineOnceDet(PengineOptions po) {
	
	}	
	// consider removing ask from PengineOptions, force it to be passed in to PengineBuilder to get an initial ask.
	// it's cleaner conceptually
	// also consider passing destroy explicitly as an optional parameter
	public Proof newPengineOnceDet(PengineOptions po, String ask) {
	
	}
	
	
	*/
	
}
