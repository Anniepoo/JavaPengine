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

import java.util.Iterator;
import java.util.Vector;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.simularity.os.javapengine.exception.PengineNotReadyException;

/**
 * @author anniepoo
 *
 * is this a public class, or a 
 */
public class Query {

	private boolean hasMore = true;  // there are more answers on the server
	private Pengine p;
	private Vector<JsonObject> availProofs = new Vector<JsonObject>();
	
	// TODO Query must call the pengine back when it's returned it's last answer so the pengine can let go
	
	/**
	 * @param pengine
	 * @param ask
	 * @param queryMaster
	 * @throws PengineNotReadyException 
	 */
	Query(Pengine pengine, String ask, boolean queryMaster) throws PengineNotReadyException {
		p = pengine;
		
		if(queryMaster) {
			p.doAsk(this, ask);
		}
	}
	
	/**
	 * @param pengine
	 * @param query
	 * @param template
	 */
	Query(Pengine pengine, String query, String template) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param pengine
	 * @param ask
	 * @throws PengineNotReadyException 
	 */
	Query(Pengine pengine, String ask) throws PengineNotReadyException {
		p = pengine;
		
		p.doAsk(this, ask);
	}

	/**
	 * return the next proof, or null if not available
	 * 
	 * Note that it is theoretically impossible to always know that there are more
	 * proofs available. Caller always needs to be ready to handle a null
	 * 
	 * It is guaranteed that if you get a null from this the query is done and will
	 * never return a non-null in the future.
	 * 
	 * @return  the next proof, or null if not available
	 * @throws PengineNotReadyException 
	 */
	public Proof next() throws PengineNotReadyException {
		// the was data available
		if(!availProofs.isEmpty()) {
			JsonObject data = availProofs.get(0);
			availProofs.remove(0);
			if(!hasMore && availProofs.isEmpty())
				p.iAmFinished(this);
			
			return new Proof(data);
		}
		
		// we don't have any available and the server's done
		if(!hasMore) {
			return null;
		}
		
		// try to get more from the server
		p.doNext(this);
		
		// if we now have data, we have to do just like above
		if(!availProofs.isEmpty()) {
			JsonObject data = availProofs.get(0);
			availProofs.remove(0);
			if(!hasMore && availProofs.isEmpty())
				p.iAmFinished(this);
			return new Proof(data);
		} else {  // we asked for data and didn't get it, the server must be done
			if(hasMore)System.err.println("Why is hasMore true here?");
			
			return null;
		}
	}
	
	// TODO make version with template

	/**
	 * signal the query that there are no more Proofs of the query available.
	 */
	void noMore() {
		if(!hasMore)  // must never call iAmFinished more than once
			return;
		
		hasMore = false;
		if(availProofs.isEmpty())
			p.iAmFinished(this);
		
		// we might be held externally, waiting to deliver last Proof or no-more-Proof result
	}

	/**
	 * @param newDataPoints
	 */
	void addNewData(JsonArray newDataPoints) {
		for(Iterator<JsonValue> iter = newDataPoints.listIterator(); iter.hasNext() ; availProofs.add( ((JsonObject)iter.next())));
	}

	// TODO make this a real iterator
	
	/**
	 * @return
	 */
	public boolean hasNext() {
		return hasMore || !availProofs.isEmpty();
	}

	/**
	 * 
	 */
	public void dumpDebugState() {
		if(this.hasMore)
			System.err.println("has more solutions");
		else
			System.err.println("no more solutions");
		
		System.err.println("availproofs" + this.availProofs.toString());
		System.err.println("pengine is " + this.p.getID());
	}
}
