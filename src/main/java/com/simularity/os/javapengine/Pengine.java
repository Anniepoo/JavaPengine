package com.simularity.os.javapengine;
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
import java.net.URLConnection;
import javax.json;

/*
 * A subclass implements the ask option
 * 
 */
public final class Pengine {
	// we copy the passed in object to make it immutable
	private final PengineOptions po;
	private final String pengineID;
	
	public Pengine(final PengineOptions poo) throws CouldNotCreateException {
		try {
			this.po = (PengineOptions) poo.clone();
		} catch (CloneNotSupportedException e) {
			throw new CouldNotCreateException("PengineOptions must be clonable");
		}
		
		pengineID = create(po);
	}
	
	private synchronized String create(PengineOptions po) throws CouldNotCreateException {
		URL url = po.getActualURL("create");
		
		URLConnection urlc = url.openConnection();
		
		Object content = urlc.getContent();
		String encoding = urlc.getContentEncoding();
		
		
	}
}
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
*/