package com.surelogic.common.java;

/**
 * Thrown when there's a conflict when merging Configs
 * 
 * @author Edwin
 */
public class MergeException extends Exception {
	private static final long serialVersionUID = 2830929516819765792L;

	public MergeException(String s) {
		super(s);
	}
}
