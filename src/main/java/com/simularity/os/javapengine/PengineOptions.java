package com.simularity.os.javapengine;

import java.net.MalformedURLException;
import java.net.URL;

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
public final class PengineOptions implements Cloneable {
	private URL server = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final PengineOptions clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (PengineOptions)super.clone();
	}

	/**
	 * @param string
	 * @return
	 */
	URL getActualURL(String string) {
		// TODO Auto-generated method stub
		// "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
		return null;
	}

	/**
	 * @return
	 */
	String getRequestBodyCreate() {
		// TODO Auto-generated method stub
		// this will be a json object with fields for options
		// sample, as a prolog dict
		//_{ src_text:"\n            q(X) :- p(X).\n            p(a). p(b). p(c).\n        "}
		return null;
	}

	/**
	 * @param urlstring
	 * @throws MalformedURLException 
	 */
	public void setServer(String urlstring) throws MalformedURLException {
		server = new URL(urlstring);
	}

}
