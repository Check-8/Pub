package ui.mongo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import ui.TodoGroup;

public class QueryCache {
	private static final String CHEF_KEY = "chef_todo_list";
	private static final int CHEF_VAL = 0;

	private Logger logger;

	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> queryResult;

	private ObjectMapperHolder omh;

	@PostConstruct
	public void init() {
		logger = LoggerFactory.getLogger(QueryCache.class);
		logger.info("INIT");
		List<ServerAddress> servers = new ArrayList<>();
		try {
			InetAddress[] inetAddresses = InetAddress.getAllByName("mongo");
			for (InetAddress inetAddress : inetAddresses) {
				ServerAddress sa = new ServerAddress(inetAddress.getHostAddress());
				servers.add(sa);
				logger.info(inetAddress.getHostAddress());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		mongoClient = new MongoClient(servers);
		db = mongoClient.getDatabase("partita");
		queryResult = db.getCollection("partita");

		omh = new ObjectMapperHolder();
	}

	@PreDestroy
	public void destroy() {
		logger.info("DESTROY");
		mongoClient.close();
	}

	public void saveChefTodoList(List<TodoGroup> pb) {
		ObjectMapper mapper = omh.getMapper();
		try {
			String jsonInString = mapper.writeValueAsString(pb);
			Document doc = Document.parse(jsonInString);
			doc.put(CHEF_KEY, CHEF_VAL);
			queryResult.insertOne(doc);
			logger.debug(jsonInString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		// TODO
	}

	public List<TodoGroup> getChefTodoList() {
		Document doc = queryResult.find(Filters.eq(CHEF_KEY, CHEF_VAL)).first();
		// TODO
		return null;
	}

	private class ObjectMapperHolder {
		private ObjectMapper objectMapper;

		public ObjectMapperHolder() {
			objectMapper = new ObjectMapper();
		}

		public ObjectMapper getMapper() {
			return objectMapper;
		}
	}
}
