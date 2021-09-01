package br.com.cp.domain.zafeiris;

public class Protocol {

	public TransportAddress buildAddress(String localHost, int localPort, Object object, Object object2) {
		return new TransportAddress(localHost, localPort);
	}

}
