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

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * @author anniepoo
 *
 */
public class Proof {
	JsonObject json;
	
	/**
	 * @param jsonValue
	 */
	Proof(JsonObject jsonValue) {
		json = jsonValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return json.toString();
	}
	
	public JsonValue getValue(String key) {
		return json.get(key);
	}
	
	public JsonObject getValues() {
		return json;
	}
	
	public String getString(String key) {
		switch (json.get(key).getValueType()) {
		case STRING:
			return ((JsonString)json.get(key)).getString();
		default:
			return json.get(key).toString();
		}
	}
	
	public int getInt(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).intValueExact();
		else
			return Integer.parseInt(getString(key));
	}
	
	public int getNearestInt(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).intValue();
		else
			return Integer.parseInt(getString(key));
	}
	
	public double getDouble(String key) {
		if (json.get(key).getValueType() == ValueType.NUMBER)
			return ((JsonNumber)json.get(key)).doubleValue();
		else
			return Double.parseDouble(getString(key));
	}
}
/*
{"X":{"args":["taco"],"functor":"a"}}
{"X":"b"}
{"X":"c"}

querying true gives an empty objectg

{}
*/