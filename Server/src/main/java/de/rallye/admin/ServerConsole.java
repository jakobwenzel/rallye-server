/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.admin;

import de.rallye.RallyeServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Felix H�bner
 * @version 1.0
 * 
 */
public class ServerConsole implements Runnable {
	
	public static final Logger logger = LogManager.getLogger(ServerConsole.class);

	private ServerSocket socket = null;
	private final RallyeServer server;

	/**
	 * create a tcp-console on localhost.
	 */
	public ServerConsole(int port, RallyeServer server) {
		this.server = server;

		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	
	public void start() {
		new Thread(this, "Console-Thread").start();
	}
	
	@Override
	public void run() {
		while (!accept());
		
		server.stopServer();
	}

	/**
	 * wait for next connection
	 * 
	 * @return return true if the server should be closed, otherwise false
	 * @author Felix H�bner
	 */
	public boolean accept() {
		logger.entry();
		Socket client = null;
		BufferedReader input = null;
		PrintWriter output = null;
		String inputLine;

		try {
			client = socket.accept();

			// logger.trace("New client on address: "+client.getLocalAddress().getHostAddress()+"");
			if (!client.getLocalAddress().getHostAddress().equals("127.0.0.1")) {
				client.close();
				logger.warn("client not allowed! close connection. Bye Bye!");
				return false;
			}

			output = new PrintWriter(client.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(
					client.getInputStream()));

			output.println("RallyeServ Telnet Server. Send ? for help.");
			logger.info("Client logged into Server Console from: "
					+ client.getInetAddress() + ":" + client.getPort());

			while ((inputLine = input.readLine()) != null) {
				if (inputLine.equals("?")) {
					output.println("Available Commands: quit,stop,status.");
				} else if (inputLine.equals("quit")) {
					logger.info("Client Disconnected.");
					output.println("Bye.");
					break;
				} else if (inputLine.equals("stop")) {
					logger.info("Stopping Server. Bye.");
					output.println("Stopping Server. Bye.");
					return logger.exit(true);
				} else if (inputLine.equals("status")) {
					logger.info("Status:");
//					output.print();//TODO: status
				} else {
					output.println("Unknown Command. Send ? for help.");
				}
			}
			input.close();
			output.close();
			client.close();
		} catch (IOException e) {
			logger.catching(e);
		}

		return logger.exit(false);
	}

	/**
	 * try to close the Game Console Socket
	 * 
	 * @author Felix H�bner
	 */
	public void close() {
		logger.entry();
		try {
			socket.close();
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.exit();
	}

}
