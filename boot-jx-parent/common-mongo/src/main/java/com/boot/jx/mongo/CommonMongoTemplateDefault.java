package com.boot.jx.mongo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ScriptOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStamp;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStamp.UpdatedTimeStampSupport;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteResult;

public abstract class CommonMongoTemplateDefault {

    protected abstract MongoTemplate getCommonMongoTemplate();

    public String getCollectionName(Class<?> entityClass) {
	return getCommonMongoTemplate().getCollectionName(entityClass);
    }

    public CommandResult executeCommand(String jsonCommand) {
	return getCommonMongoTemplate().executeCommand(jsonCommand);
    }

    public CommandResult executeCommand(DBObject command) {
	return getCommonMongoTemplate().executeCommand(command);
    }

    @SuppressWarnings("deprecation")
    public CommandResult executeCommand(DBObject command, int options) {
	return getCommonMongoTemplate().executeCommand(command, options);
    }

    public CommandResult executeCommand(DBObject command, ReadPreference readPreference) {
	return getCommonMongoTemplate().executeCommand(command, readPreference);
    }

    public void executeQuery(Query query, String collectionName, DocumentCallbackHandler dch) {
	getCommonMongoTemplate().executeQuery(query, collectionName, dch);

    }

    public <T> T execute(DbCallback<T> action) {
	return getCommonMongoTemplate().execute(action);
    }

    public <T> T execute(Class<?> entityClass, CollectionCallback<T> action) {
	return getCommonMongoTemplate().execute(entityClass, action);
    }

    public <T> T execute(String collectionName, CollectionCallback<T> action) {
	return getCommonMongoTemplate().execute(collectionName, action);
    }

    @SuppressWarnings("deprecation")
    public <T> T executeInSession(DbCallback<T> action) {
	return getCommonMongoTemplate().executeInSession(action);
    }

    public <T> CloseableIterator<T> stream(Query query, Class<T> entityType) {
	return getCommonMongoTemplate().stream(query, entityType);
    }

    public <T> DBCollection createCollection(Class<T> entityClass) {
	return getCommonMongoTemplate().createCollection(entityClass);
    }

    public <T> DBCollection createCollection(Class<T> entityClass, CollectionOptions collectionOptions) {
	return getCommonMongoTemplate().createCollection(entityClass, collectionOptions);
    }

    public DBCollection createCollection(String collectionName) {
	return getCommonMongoTemplate().createCollection(collectionName);
    }

    public DBCollection createCollection(String collectionName, CollectionOptions collectionOptions) {
	return getCommonMongoTemplate().createCollection(collectionName, collectionOptions);
    }

    public Set<String> getCollectionNames() {
	return getCommonMongoTemplate().getCollectionNames();
    }

    public DBCollection getCollection(String collectionName) {
	return getCommonMongoTemplate().getCollection(collectionName);
    }

    public <T> boolean collectionExists(Class<T> entityClass) {
	return getCommonMongoTemplate().collectionExists(entityClass);
    }

    public boolean collectionExists(String collectionName) {
	return getCommonMongoTemplate().collectionExists(collectionName);
    }

    public <T> void dropCollection(Class<T> entityClass) {
	getCommonMongoTemplate().dropCollection(entityClass);
    }

    public void dropCollection(String collectionName) {
	getCommonMongoTemplate().dropCollection(collectionName);

    }

    public IndexOperations indexOps(String collectionName) {
	return getCommonMongoTemplate().indexOps(collectionName);
    }

    public IndexOperations indexOps(Class<?> entityClass) {
	return getCommonMongoTemplate().indexOps(entityClass);
    }

    public ScriptOperations scriptOps() {
	return getCommonMongoTemplate().scriptOps();
    }

    public BulkOperations bulkOps(BulkMode mode, String collectionName) {
	return getCommonMongoTemplate().bulkOps(mode, collectionName);
    }

    public BulkOperations bulkOps(BulkMode mode, Class<?> entityType) {
	return getCommonMongoTemplate().bulkOps(mode, entityType);
    }

    public BulkOperations bulkOps(BulkMode mode, Class<?> entityType, String collectionName) {
	return getCommonMongoTemplate().bulkOps(mode, entityType, collectionName);
    }

    public <T> List<T> findAll(Class<T> entityClass) {
	return getCommonMongoTemplate().findAll(entityClass);
    }

    public <T> List<T> findAll(Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findAll(entityClass, collectionName);
    }

    public <T> GroupByResults<T> group(String inputCollectionName, GroupBy groupBy, Class<T> entityClass) {
	return getCommonMongoTemplate().group(inputCollectionName, groupBy, entityClass);
    }

    public <T> GroupByResults<T> group(Criteria criteria, String inputCollectionName, GroupBy groupBy,
	    Class<T> entityClass) {
	return getCommonMongoTemplate().group(criteria, inputCollectionName, groupBy, entityClass);
    }

    public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, String collectionName,
	    Class<O> outputType) {
	return getCommonMongoTemplate().aggregate(aggregation, outputType);
    }

    public <O> AggregationResults<O> aggregate(TypedAggregation<?> aggregation, Class<O> outputType) {
	return getCommonMongoTemplate().aggregate(aggregation, outputType);
    }

    public <O> AggregationResults<O> aggregate(Aggregation aggregation, Class<?> inputType, Class<O> outputType) {
	return getCommonMongoTemplate().aggregate(aggregation, inputType, outputType);
    }

    public <O> AggregationResults<O> aggregate(Aggregation aggregation, String collectionName, Class<O> outputType) {
	return getCommonMongoTemplate().aggregate(aggregation, collectionName, outputType);
    }

    public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
	    Class<T> entityClass) {
	return getCommonMongoTemplate().mapReduce(inputCollectionName, mapFunction, reduceFunction, entityClass);
    }

    public <T> MapReduceResults<T> mapReduce(String inputCollectionName, String mapFunction, String reduceFunction,
	    MapReduceOptions mapReduceOptions, Class<T> entityClass) {
	return getCommonMongoTemplate().mapReduce(inputCollectionName, mapFunction, reduceFunction, mapReduceOptions,
		entityClass);
    }

    public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
	    String reduceFunction, Class<T> entityClass) {
	return getCommonMongoTemplate().mapReduce(query, inputCollectionName, mapFunction, reduceFunction, entityClass);
    }

    public <T> MapReduceResults<T> mapReduce(Query query, String inputCollectionName, String mapFunction,
	    String reduceFunction, MapReduceOptions mapReduceOptions, Class<T> entityClass) {
	return mapReduce(query, inputCollectionName, mapFunction, reduceFunction, mapReduceOptions, entityClass);
    }

    public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass) {
	return getCommonMongoTemplate().geoNear(near, entityClass);
    }

    public <T> GeoResults<T> geoNear(NearQuery near, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().geoNear(near, entityClass, collectionName);
    }

    public <T> T findOne(Query query, Class<T> entityClass) {
	return getCommonMongoTemplate().findOne(query, entityClass);
    }

    public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findOne(query, entityClass, collectionName);
    }

    public boolean exists(Query query, String collectionName) {
	return getCommonMongoTemplate().exists(query, collectionName);
    }

    public boolean exists(Query query, Class<?> entityClass) {
	return getCommonMongoTemplate().exists(query, entityClass);
    }

    public boolean exists(Query query, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().exists(query, entityClass, collectionName);
    }

    public <T> List<T> find(Query query, Class<T> entityClass) {
	return getCommonMongoTemplate().find(query, entityClass);
    }

    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().find(query, entityClass, collectionName);
    }

    public <T> T findById(Object id, Class<T> entityClass) {
	return getCommonMongoTemplate().findById(id, entityClass);
    }

    public <T> T findById(Object id, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findById(id, entityClass, collectionName);
    }

    public <T> T findAndModify(Query query, Update update, Class<T> entityClass) {
	return getCommonMongoTemplate().findAndModify(query, update, entityClass);
    }

    public <T> T findAndModify(Query query, Update update, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findAndModify(query, update, entityClass, collectionName);
    }

    public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass) {
	return getCommonMongoTemplate().findAndModify(query, update, options, entityClass);
    }

    public <T> T findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass,
	    String collectionName) {
	return getCommonMongoTemplate().findAndModify(query, update, options, entityClass, collectionName);
    }

    public <T> T findAndRemove(Query query, Class<T> entityClass) {
	return getCommonMongoTemplate().findAndRemove(query, entityClass);
    }

    public <T> T findAndRemove(Query query, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findAndRemove(query, entityClass, collectionName);
    }

    public long count(Query query, Class<?> entityClass) {
	return getCommonMongoTemplate().count(query, entityClass);
    }

    public long count(Query query, String collectionName) {
	return getCommonMongoTemplate().count(query, collectionName);
    }

    public long count(Query query, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().count(query, entityClass, collectionName);
    }

    public void insert(Object objectToSave) {
	getCommonMongoTemplate().insert(objectToSave);
    }

    public void insert(Object objectToSave, String collectionName) {
	getCommonMongoTemplate().insert(objectToSave, collectionName);
    }

    public void insert(Collection<? extends Object> batchToSave, Class<?> entityClass) {
	getCommonMongoTemplate().insert(batchToSave, entityClass);
    }

    public void insert(Collection<? extends Object> batchToSave, String collectionName) {
	getCommonMongoTemplate().insert(batchToSave, collectionName);
    }

    public void insertAll(Collection<? extends Object> objectsToSave) {
	getCommonMongoTemplate().insertAll(objectsToSave);
    }

    public void save(Object objectToSave) {
	if (objectToSave instanceof UpdatedTimeStampSupport) {
	    ((UpdatedTimeStampSupport) objectToSave).setUpdated(TimeStamp.now());
	}
	getCommonMongoTemplate().save(objectToSave);
    }

    public void save(Object objectToSave, String collectionName) {
	if (objectToSave instanceof UpdatedTimeStampSupport) {
	    ((UpdatedTimeStampSupport) objectToSave).setUpdated(TimeStamp.now());
	}
	getCommonMongoTemplate().save(objectToSave, collectionName);
    }

    public WriteResult upsert(Query query, Update update, Class<?> entityClass) {
	return getCommonMongoTemplate().upsert(query, update, entityClass);
    }

    public WriteResult upsert(Query query, Update update, String collectionName) {
	return getCommonMongoTemplate().upsert(query, update, collectionName);
    }

    public WriteResult upsert(Query query, Update update, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().upsert(query, update, entityClass, collectionName);
    }

    public WriteResult updateFirst(Query query, Update update, Class<?> entityClass) {
	return getCommonMongoTemplate().updateFirst(query, update, entityClass);
    }

    public WriteResult updateFirst(Query query, Update update, String collectionName) {
	return getCommonMongoTemplate().updateFirst(query, update, collectionName);
    }

    public WriteResult updateFirst(Query query, Update update, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().updateFirst(query, update, entityClass, collectionName);
    }

    public WriteResult updateMulti(Query query, Update update, Class<?> entityClass) {
	return getCommonMongoTemplate().updateMulti(query, update, entityClass);
    }

    public WriteResult updateMulti(Query query, Update update, String collectionName) {
	return getCommonMongoTemplate().updateMulti(query, update, collectionName);
    }

    public WriteResult updateMulti(Query query, Update update, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().updateMulti(query, update, entityClass, collectionName);
    }

    public WriteResult remove(Object object) {
	return getCommonMongoTemplate().remove(object);
    }

    public WriteResult remove(Object object, String collection) {
	return getCommonMongoTemplate().remove(object, collection);
    }

    public WriteResult remove(Query query, Class<?> entityClass) {
	return getCommonMongoTemplate().remove(query, entityClass);
    }

    public WriteResult remove(Query query, Class<?> entityClass, String collectionName) {
	return getCommonMongoTemplate().remove(query, entityClass, collectionName);
    }

    public WriteResult remove(Query query, String collectionName) {
	return getCommonMongoTemplate().remove(query, collectionName);
    }

    public <T> List<T> findAllAndRemove(Query query, String collectionName) {
	return getCommonMongoTemplate().findAllAndRemove(query, collectionName);
    }

    public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass) {
	return getCommonMongoTemplate().findAllAndRemove(query, entityClass);
    }

    public <T> List<T> findAllAndRemove(Query query, Class<T> entityClass, String collectionName) {
	return getCommonMongoTemplate().findAllAndRemove(query, entityClass, collectionName);
    }

    public MongoConverter getConverter() {
	return getCommonMongoTemplate().getConverter();
    }

    public DB getDb() {
	return getCommonMongoTemplate().getDb();
    }

}
