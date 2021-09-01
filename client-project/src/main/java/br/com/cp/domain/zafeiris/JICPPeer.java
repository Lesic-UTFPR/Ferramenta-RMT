package br.com.cp.domain.zafeiris;

import br.com.cp.domain.zafeiris.ICP.Listener;

public class JICPPeer {
	
	private final int CONNECTION_TIMEOUT = 120;
	private final int POOL_SIZE = 5;
	private String myID = null;
	private int connectionTimeout = 20;
	private JICPClient client = null;
	private JICPServer server = null;
	private Ticket ticket = null;
	
	public TransportAddress activate(Listener i, String peerId, Profile p) throws ICPException{
		
		myID = peerId;
		
		connectionTimeout = p.getparameter(CONNECTION_TIMEOUT, 0);
		client = new JICPClient(getProtocol(), getConnectionFactory(), POOL_SIZE);
		server = new JICPServer(p, this, i, getConnectionFactory(), POOL_SIZE);
		server.start();
		ticket = new Ticket(60000);
		ticket.start();
		
		TransportAddress localIta = getProtocol().buildAddress(server.getLocalHost(), server.getLocalPort(), null, null);
		
		return localIta;
	}

	private Protocol getProtocol() {
		return null;
	}
	
	private ConnectionFactory getConnectionFactory() {
		return null;
	}

}
