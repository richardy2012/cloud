package com.csf.cloud.dao.mongo;

import com.csf.cloud.bloomfilter.CanGenerateHashFrom;
import com.csf.cloud.bloomfilter.mutable.BloomFilter;
import com.csf.cloud.config.DefaultConfiguration;
import com.csf.cloud.dao.BaseDao;
import com.csf.cloud.storage.Storage;
import com.csf.cloud.storage.Storage$;
import com.csf.cloud.util.JavaLogging;
import com.csf.cloud.util.Utils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;
import org.slf4j.Logger;
import scala.Char;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by soledede.weng on 2016/6/28.
 */
public abstract class MongoBaseDao<T> extends BaseDao implements DefaultConfiguration {
    private Logger log = JavaLogging.log();
    BloomFilter<String> bloomFilter = BloomFilter.apply("unqiue_primary_key", expectedElements(),
            falsePositiveRate(),
            CanGenerateHashFrom.CanGenerateHashFromString$.MODULE$);
    Storage storage = Storage$.MODULE$.apply("redis");

    protected Boolean saveOrUpdate(T entity, Datastore ds) {
        if (ds == null) return false;
        try {
            Class clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();
            String objectId = null;
            if (fields != null) {
                for (Field field : fields) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Id annon = field.getAnnotation(Id.class);
                    if (annon != null) {
                        Method idGet = clazz.getMethod(get(field.getName()));
                        if (idGet != null) objectId = idGet.invoke(entity).toString();
                        break;
                    }
                }
            }

            if (objectId != null) {
                Boolean checkUpdate = bloomFilter.mightContain(objectId);
                if (!checkUpdate) {//need save all field of obj to mongo
                    ds.save(entity);
                } else {
                    //check some field ,whether is updated by some guys
                    String fieldsForObjectId = storage.getStringBykey(objectId);
                    if (fieldsForObjectId == null || fieldsForObjectId.equalsIgnoreCase("")) {
                        //This is false positive of bloomFilter
                        ds.save(entity);
                    } else {
                        String[] reDuplicateFieleds = fieldsForObjectId.split(checkSeparator());
                        //set these filds for null
                        for (String dupFieldName : reDuplicateFieleds) {
                            Method setField = clazz.getMethod(set(dupFieldName));
                            setField.invoke(entity, null);
                        }
                        ds.merge(entity);
                    }
                }
                return true;
            } else {
                log.error("No annotation @Id for entity!");
                return false;
            }
        } catch (NoSuchMethodException e) {
            log.error("no  method for reflect!", e.getCause());
            return false;
        } catch (Exception e) {
            log.error("faield!", e.getCause());
            return false;
        }
    }

    private String get(String fieldName) {
        return "get" + Utils.toUpperCaseFirstOne(fieldName);
    }

    private String set(String fieldName) {
        return "set" + Utils.toUpperCaseFirstOne(fieldName);
    }
}
