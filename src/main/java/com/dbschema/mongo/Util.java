package com.dbschema.mongo;

import com.dbschema.mongo.resultSet.ListResultSet;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static java.util.Collections.emptyList;

/**
 * @author Liudmila Kornilova
 **/
public class Util {
  public static String nullize(String text) {
    return text == null || text.isEmpty() ? null : text;
  }

  public static ResultSet ok() {
    return new ListResultSet("OK", new String[]{"result"});
  }

  public static ResultSet ok(Object result) {
    return new ListResultSet(result, new String[]{"result"});
  }

  public static ResultSet ok(Document result) {
    return new ListResultSet(result, new String[]{"map"});
  }

  public static ResultSet error() {
    return new ListResultSet("ERROR", new String[]{"result"});
  }

  @Contract(pure = true)
  public static <T> T find(@NotNull Iterable<? extends T> iterable, @NotNull Condition<? super T> condition) {
    return find(iterable.iterator(), condition);
  }

  public static <T> T find(@NotNull Iterator<? extends T> iterator, @NotNull Condition<? super T> condition) {
    while (iterator.hasNext()) {
      T value = iterator.next();
      if (condition.value(value)) return value;
    }
    return null;
  }

  @NotNull
  @Contract(pure = true)
  public static <T> T[] filter(@NotNull T[] collection, @NotNull Condition<? super T> condition) {
    List<T> result = new ArrayList<>();
    for (T t : collection) {
      if (condition.value(t)) {
        result.add(t);
      }
    }
    //noinspection unchecked
    return ((T[]) result.toArray());
  }

  @NotNull
  @Contract(pure = true)
  public static <T, R> R[] map(@NotNull T[] collection, @NotNull Function<? super T, R> func) {
    List<R> result = new ArrayList<>();
    for (T t : collection) {
      result.add(func.apply(t));
    }
    //noinspection unchecked
    return ((R[]) result.toArray());
  }

  @Nullable
  @Contract("!null -> !null")
  public static Document toDocument(@Nullable BsonDocument bson) {
    return bson == null ? null : getDefaultCodecRegistry().get(Document.class).decode(bson.asBsonReader(), DecoderContext.builder().build());
  }

  @Nullable
  @Contract("!null -> !null")
  public static Object decode(@Nullable BsonValue bson) {
    return bson == null ? null :
           getDefaultCodecRegistry().get(Document.class).decode(new BsonDocument("v", bson).asBsonReader(), DecoderContext.builder().build()).get("v");
  }

  @Contract(value = "null, _ -> null", pure = true)
  @Nullable
  public static <T> T tryCast(@Nullable Object obj, @NotNull Class<T> clazz) {
    if (clazz.isInstance(obj)) {
      return clazz.cast(obj);
    }
    return null;
  }

  @NotNull
  @Contract(pure=true)
  public static <T,V> List<V> map(@NotNull Collection<? extends T> collection, @NotNull Function<? super T, ? extends V> mapping) {
    if (collection.isEmpty()) return emptyList();
    List<V> list = new ArrayList<>(collection.size());
    for (final T t : collection) {
      list.add(mapping.apply(t));
    }
    return list;
  }

  @NotNull
  @Contract(pure=true)
  public static <T, V> List<V> mapNotNull(@NotNull Collection<? extends T> collection, @NotNull Function<? super T, ? extends V> mapping) {
    if (collection.isEmpty()) {
      return emptyList();
    }

    List<V> result = new ArrayList<>(collection.size());
    for (T t : collection) {
      final V o = mapping.apply(t);
      if (o != null) {
        result.add(o);
      }
    }
    return result.isEmpty() ? emptyList() : result;
  }
}
