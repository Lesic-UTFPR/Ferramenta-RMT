package br.com.cp.domain.zafeiris;

import br.com.cp.domain.zafeiris.ICP.Listener;

public class JICPSPeer extends JICPPeer{
	private MyLogger myLogger = null;
	private SSLContext ctx = null;

	@Override
	public TransportAddress activate(Listener i, String peerId, Profile p) throws ICPException {
		
		if(myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "About to Activate JICP peer.");
		}
		
		ctx = SSLHelper.createContext();
		
		setUserSSLAuth(SSLHelper.needAuth());
		
		if(myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "activate(0 context created ctx=" + ctx);
		}
		
		TransportAddress ta = super.activate(i, peerId, p);
		
		if(myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "JICP Secure Peer activated");
		}
		
		return ta;
	}

	private void setUserSSLAuth(boolean needAuth) {
	}
	
}
