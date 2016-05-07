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

import java.util.ArrayList;
/**
 * @author anniepoo
 *
 */
public class PengineState {
	public static enum PSt {
		NOT_CREATED,   // state 0 in Torbjorns diagram
		IDLE,  			// state 2 in Torbjorns diagram
		ASK,			// state 3 in Torbjorns diagram
		DESTROYED		// state 1, but we don't provide a way to get to state 2 from here
		
	};
	
	private static class Transition {
		public PSt from;
		public PSt to;
		
		Transition(PSt f, PSt t) {
			from = f;
			to = t;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			
			return super.hashCode() + to.hashCode() + from.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj == null || !(obj instanceof Transition))
				return false;
			
			Transition ot = (Transition)obj;
			
			return (ot.from.equals(this.from) && ot.to.equals(this.to));
		}
	}
	
	private static ArrayList<Transition> allowed = new ArrayList<Transition>();
	
	static {
			allowed.add(new Transition(PSt.NOT_CREATED, PSt.IDLE));
			allowed.add(new Transition(PSt.NOT_CREATED, PSt.ASK));
			allowed.add(new Transition(PSt.IDLE, PSt.ASK));
			allowed.add(new Transition(PSt.ASK, PSt.IDLE));	
			allowed.add(new Transition(PSt.IDLE, PSt.DESTROYED));
			allowed.add(new Transition(PSt.ASK, PSt.DESTROYED));
			allowed.add(new Transition(PSt.NOT_CREATED, PSt.DESTROYED)); // if we can't create it
	}
	
	private PSt state = PSt.NOT_CREATED;
	
	/**
	 * 
	 */
	public PengineState() {
		super();
	}
	
	public void setState(PSt newstate) throws PengineNotReadyException {
		if(newstate.equals(state))
			return;
		
		if(allowed.contains(new Transition(this.state, newstate)))  {
			state = newstate;
		} else {
			throw new PengineNotReadyException("Darn it can't transition from" + this.state.toString() + " to " + newstate.toString());
		}
	}
	
	public PSt getState() {
		return state;
	}

	/**
	 * @param destroyed
	 */
	public boolean isIn(PSt aState) {
		return this.state.equals(aState);
		
	}

	/**
	 * throw an IllegalStateException if we're not in
	 * 
	 * @param aState 
	 * @throws PengineNotReadyException 
	 */
	public void must_be_in(PSt aState) throws PengineNotReadyException {
		if(!isIn(aState))
			throw new PengineNotReadyException("Should be in " + aState.toString() + ", but is in " + state.toString());
		
	}

	/**
	 * @param aState
	 * @param anotherState
	 */
	public void must_be_in(PSt aState, PSt anotherState) {
		if(!isIn(aState) && !isIn(anotherState))
			throw new IllegalStateException("Should be in " + aState.toString() + ", but is in " + state.toString());
		
	}

	/**
	 * pengines can always be destroyed via this method
	 * 
	 */
	void destroy() {
		this.state = PSt.DESTROYED; 
		
	}
	
	
}
