package com.simularity.os.javapengine;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
public final class PengineOptions implements Cloneable {
	private URL server = null;
	
	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final PengineOptions clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (PengineOptions)super.clone();
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
	URL getActualURL(String action) throws CouldNotCreateException {
		if(server == null) {
			throw new CouldNotCreateException("Cannot get actual URL without setting server");
		}
		try {		
			URI uribase = server.toURI();
			if (uribase.isOpaque()) {
				throw new CouldNotCreateException("Cannot get actual URL without setting server");
			}
			
			URI relative = new URI("/pengines/" + action);
			
			URI fulluri = uribase.resolve(relative);

			return fulluri.toURL();
		} catch (MalformedURLException e) {
			throw new CouldNotCreateException("Cannot form actual URL for action " + action + " from uri " + fulluri.toString());
		}
	}

	/**
	 * @return a string representation of the request body for the create action
	 */
	String getRequestBodyCreate() {
		// TODO Auto-generated method stub
		// this will be a json object with fields for options
		// sample, as a prolog dict
		//_{ src_text:"\n            q(X) :- p(X).\n            p(a). p(b). p(c).\n        "}
		return null;
	}

	/**
	 * @param urlstring String that represents the server URL - this does not contain the /pengines/create extension
	 * @throws MalformedURLException 
	 */
	public void setServer(String urlstring) throws MalformedURLException {
		server = new URL(urlstring);
	}

}
