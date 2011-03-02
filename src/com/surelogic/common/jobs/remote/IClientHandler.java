package com.surelogic.common.jobs.remote;

/**
 * A thread to handle request from console connections. More than one client
 * can be connected so there could be several instance of this class.
 */
public interface IClientHandler {
	String getName();
	void start();
	void requestShutdown();
}
