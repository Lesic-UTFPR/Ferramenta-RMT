package br.com.cp.domain.zafeiris;

import br.com.cp.domain.zafeiris.ICP.Listener;

public class JICPServer {

	public JICPServer(Profile p, JICPPeer jicpPeer, Listener i, ConnectionFactory connectionFactory, int poolSize) {
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public String getLocalHost() {
		return "127.0.0.1";
	}

	public int getLocalPort() {
		return 8080;
	}

}
