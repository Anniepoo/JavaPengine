package com.simularity.os.javapengine;

import java.net.URL;
import java.net.URLConnection;
import javax.json;

/*
 * A subclass implements the ask option
 * 
 */
public class Pengine {
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