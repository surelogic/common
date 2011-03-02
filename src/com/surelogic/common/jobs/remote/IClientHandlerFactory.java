package com.surelogic.common.jobs.remote;

import java.net.Socket;

public interface IClientHandlerFactory {
	IClientHandler newHandler(Console console, Socket client);
}
