/**
 * Copyright (c) 2016 Simularity Inc.
 * 
 * FIXME
 * 
 */
package com.simularity.os.javapengine;

import java.net.URL;

/**
 * @author Annie
 *
 * Pengine factory.
 * 
 * Usage - 
 */
public class PengineFactory {
	private static PengineFactory defaultPengineFactory = null;
	
	/**
	 * 
	 */
	package PengineFactory() {
		
	}
	
	public static PengineFactory getDefaultPengineFactory() {
		if (defaultPengineFactory == null) {
			defaultPengineFactory = new PengineFactory();
		}
		
		return defaultPengineFactory;
	}
	
	public Pengine newPengine() {
		return new Pengine();
	}
	
	public Pengine newPengine(URL server, String content) {
		PengineOptions opts = new PengineOptions();
		
		
	}
}
