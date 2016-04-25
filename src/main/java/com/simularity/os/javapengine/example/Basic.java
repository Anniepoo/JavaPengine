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
package com.simularity.os.javapengine.example;

import java.net.MalformedURLException;

import com.simularity.os.javapengine.CouldNotCreateException;
import com.simularity.os.javapengine.Pengine;
import com.simularity.os.javapengine.PengineFactory;
import com.simularity.os.javapengine.PengineOptions;

/**
 * @author anniepoo
 *
 */
public final class Basic {

	/**
	 * 
	 */
	public Basic() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PengineOptions po = new PengineOptions();
		try {
			po.setServer("http://localhost:9900/");
			Pengine p = PengineFactory.d().newPengine(po);
		} catch (MalformedURLException e) {
			System.err.println("Bad URL" + e.getMessage());
			e.printStackTrace();
		} catch (CouldNotCreateException e) {
			System.err.println("cannot make pengine" + e.getMessage());
			e.printStackTrace();
		}
	}

}
