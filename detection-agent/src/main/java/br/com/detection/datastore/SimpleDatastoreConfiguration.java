package br.com.detection.datastore;

public class SimpleDatastoreConfiguration implements DatastoreConfiguration {

	private static final long serialVersionUID = 1L;

	@Override
	public String getHost() {
		return "127.0.0.1";
	}

	@Override
	public int getPort() {
		return 27017;
	}

	@Override
	public String getDatabaseName() {
		return "archprome";
	}

}
