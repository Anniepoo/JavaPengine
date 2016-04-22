package com.simularity.os.javapengine;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.simularity.os.javapengine.prolog.parser.CompoundTerm;
import com.simularity.os.javapengine.prolog.parser.PrologParser;
import com.simularity.os.javapengine.prolog.parser.PrologTerm;

/*
 * A subclass implements the ask option
 * 
 */
public final class Pengine {
	// we copy the passed in object to make it immutable
	private final PengineOptions po;
	@SuppressWarnings("unused")
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
		StringBuffer response;

		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// above should get us an HttpsURLConnection if it's https://...

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "JavaPengine");
			con.setRequestProperty("Accept", "application/json application/x-prolog text/x-prolog");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

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
			System.err.println("\nSending 'POST' request to URL : " + url);
			System.err.println("Post parameters : " + urlParameters);
			System.err.println("Response Code : " + responseCode);

			// response will look like 
			// create('6bd9513c-efcb-4fd7-a7ea-ae1ecdd51013',[slave_limit(3)]).
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
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new CouldNotCreateException(e.toString());
				}
			}

			System.err.println(response.toString());

			PrologTerm resp = PrologParser.parse(response.toString());
			
			if(resp instanceof CompoundTerm)
				return ((CompoundTerm) resp).arg(1).toString();
			else
				throw new CouldNotCreateException("server returned a non-compound term");
			
		} catch (IOException | SyntaxErrorException e) {
			throw new CouldNotCreateException(e.toString());
		}
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