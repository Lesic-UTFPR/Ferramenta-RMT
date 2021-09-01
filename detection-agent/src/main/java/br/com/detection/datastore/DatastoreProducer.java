package br.com.detection.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Singleton
public class DatastoreProducer implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Map<DatastoreConfiguration, Connection> CONNECTION_CACHE = new HashMap<>();

	private final DatastoreConfiguration config = new SimpleDatastoreConfiguration();

	private Connection getConnection() {
		return CONNECTION_CACHE.computeIfAbsent(this.config, c -> new Connection(c));
	}

	@Produces
	public Datastore getDatastore() {
		return this.getConnection().getDS();
	}

	@Produces
	public Morphia getMorphia() {
		return this.getConnection().getMorphia();
	}

	@Produces
	public Mongo getMongo() {
		return this.getConnection().getDS().getMongo();
	}

	@PreDestroy
	public void close() {
		this.getMongo().close();
		CONNECTION_CACHE.remove(this.config);
	}

	private static final class Connection {

		private final Morphia morphia;

		private final Datastore ds;

		public Connection(final DatastoreConfiguration config) {
			this.morphia = new Morphia();
			this.ds = this.getMorphia().createDatastore(new MongoClient(new ArrayList<>(config.getServerAddress()),
					new ArrayList<>(config.getCredentials())), config.getDatabaseName());
			this.getDS().ensureIndexes();
		}

		public Morphia getMorphia() {
			return this.morphia;
		}

		public Datastore getDS() {
			return this.ds;
		}
	}

}
