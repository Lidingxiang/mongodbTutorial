package com.yq.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.yq.mongo.config.DBConfig;
import com.yq.mongo.conn.MongoDBConn;
import com.yq.mongo.crud.Operation;

// you can use eq or gte
//import static com.mongodb.client.model.Filters.*;
/**
 * Hello world!
 *
 */
public class App {
    //**************************************************************************
    // CLASS
    //**************************************************************************
    private static final Logger log = Logger.getLogger(App.class);
    
    Block<Document> printBlock = new Block<Document>() {
        public void apply(final Document document) {
            System.out.println(document.toJson());
        }
    };

    public static void main( String[] args ) {
        MongoDBConn conn = new MongoDBConn(DBConfig.getInstance());
        String collectionName = "col1";
        MongoCollection<Document> coll = conn.getCollection(collectionName);
        if (coll != null) {
            //db.runCommand( { buildInfo: 1 } )
            //db.adminCommand( { listDatabases: 1 } )
            Document buildInfoResults = conn.getDB().runCommand(new Document("buildInfo", 1));
            log.info("buildInfo:" + buildInfoResults.toJson());

            //https://docs.mongodb.com/manual/reference/command/
            //Document("db.col1.find()", 1))  can't work
            Document isMasterResults = conn.getDB().runCommand(new Document("isMaster", 1));
            log.info("isMasterResults:" + isMasterResults.toJson());

            //db.runCommand({find: "col1"})
            Document findResults = conn.getDB().runCommand(new Document("find", "col1"));
            log.info("findResults:" + findResults.toJson());

            Operation operation = new Operation();

            Document document = new Document("title", "Java Core v9")
                .append("description", "guide for java9")
                .append("category", "book")
                .append("id", 99)
                .append("price", 100.65)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("isPublished", true)
                .append("Author", "ericYang");

            List<Document> docList = new ArrayList<Document>();
            for (int i=0; i < 2; i++) {
                Document doc = new Document("id", i)
                    .append("description", "doc array demo")
                    //.append("category", "book")
                    .append("text", "test only");
                docList.add(doc);
            }
            document.append("docArray", docList);
            operation.insert(coll, document);

            List<String> idList = operation.getAllDocs(coll);
            //we insert a doc, so idList has 1 element at least.
            String id = idList.get(0);
            Document doc = operation.findById(coll, id);
            System.out.println("Find by id '" + id + ". Result:" + doc);

            Document newDoc = new Document("title", "JavaSE Core 9 ").
            //原来有title， 我们相当于修改了title， 原来没有的sales volume，相当于添加新的field。
                append("sales volume", 3000).
                append("price", 100).
                append("Author", "ericYang");
            operation.updateById(coll, id, newDoc);
            doc = operation.findById(coll, id);
            System.out.println("Find by id '" + id + " after updating. Result:" + doc);

            int count = operation.deleteById(coll, id);
            System.out.println("delete by id '" + id + "'. AffectedRowCount:" + count);

            System.out.println("Show all docs");
            operation.getAllDocs(coll);

            log.info("query specific fields");
            Document fields = new Document("title", 1)
                                  .append("price", 2);
            operation.querySpecifiedFields(coll, fields);

            App app = new App();
            app.insertMany(coll);
            app.queryAndSort(coll);
            app.aggregateDemo(coll);

            System.out.println("Show all docs");
            operation.getAllDocs(coll);
            app.deleteMany(coll);
            conn.closeMongoClient();
        }
        else {
            log.info("Can't find collection '" + collectionName + "'.");
        }
    }

    public void insertMany(MongoCollection<Document> coll) {
        List<Document> docList = new ArrayList<Document>();
        for (int i=0; i < 10; i++) {
            Document doc = new Document("id", i)
                .append("description", "doc demo")
                .append("index", 100 -i)
                .append("category", "video")
                .append("price", i + 10);
            docList.add(doc);
        }
        coll.insertMany(docList);
    }

    public void deleteMany(MongoCollection<Document> coll) {
        Bson filter = Filters.gte("id", 0);
        DeleteResult deleteResult = coll.deleteMany(filter);
        System.out.println("delete " + deleteResult.getDeletedCount() + " document(s).");
    }

    public void queryAndSort(MongoCollection<Document> coll) {
       Document findQuery = new Document("id", new Document("$gte",6));
       //"price", 1 表示按照price升序， -1表示降序
       Document orderBy = new Document("price", -1);

       System.out.println("order by price");
       MongoCursor<Document> cursor = coll.find(findQuery).sort(orderBy).iterator();
       try {
           while (cursor.hasNext()) {
               Document doc = cursor.next();
               System.out.println(
                   "Id " + doc.get("id") + ", price:" + doc.get("price") + " ."
               );
           }
       } finally {
           cursor.close();
       }
    }

    public void aggregateDemo(MongoCollection<Document> coll) {
        //select category, count(*) as count from `col1` group by category and id >=0;
        //AggregateIterable<Document>
        System.out.println("aggregate query");
        coll.aggregate(
            Arrays.asList(
                    Aggregates.match(Filters.gte("id", 0)),
                    Aggregates.group("$category", Accumulators.sum("count", 1))
            )
          ).forEach(printBlock);
        
        /*
         * { "_id" : "video", "count" : 10 }
           { "_id" : "book", "count" : 1 }
         */
    }

}
