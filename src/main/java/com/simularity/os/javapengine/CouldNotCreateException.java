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
/**
 * Thrown whenever a Pengine cannot be created.
 * 
 * JavaPengine is designed to reduce the amount of exception handling the user needs to do.
 *
 * The most common reason a Pengine cannot be created is inability to contact the server.
 * The second most common reason is a bad set of PengineOptions, either bad values or because
 * clone throws CloneNotSupportedException
 * 
 * @author Anne Ogborn
 *
 */
public class CouldNotCreateException extends Exception {
	private String message;
	
	/**
	 * @param message  the message returned by getMessage()
	 */
	public CouldNotCreateException(String message) {
		this.message = message;
	}

	/**
	 * Returns a descriptive message 
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * Convert to a string - just returns the message
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return message;
	}

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 2600990953636107225L;
	
}
  