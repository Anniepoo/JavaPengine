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

/**
 * @author Annie
 *
 * Pengine factory.
 * 
 * Usage - 
 */
public class PengineFactory {
	private static PengineFactory defaultPengineFactory = null;
	private PengineOptions po = null;
	/**
	 * 
	 */
	PengineFactory() {
		
	}
	
	public static PengineFactory getDefaultPengineFactory() {
		if (defaultPengineFactory == null) {
			defaultPengineFactory = new PengineFactory();
		}
		
		return defaultPengineFactory;
	}
	
	public static PengineFactory d() {
		return getDefaultPengineFactory();
	}
	
	public void setDefaultOptions(PengineOptions po) {
		try {
			this.po = po.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public PengineOptions getDefaultOptions() {
		try {
			return po.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null; // keeps compiler happy.
		}
	}
	
	public Pengine newPengine() throws CouldNotCreateException {
		return new Pengine(this.po);
	}
	
	public Pengine newPengine(PengineOptions po) throws CouldNotCreateException {
		return new Pengine(po);
	}
	
	/* eventually we have this, and subclass Pengine with PengineOnce and PengineMany, which return Query
	public Query newPengineOnce(String ask) {
		return newPengineOnce(this.po, ask);
	}
	
	public Query newPengineOnce(PengineOptions po, String ask) {
				
	}
	*/
	
}
