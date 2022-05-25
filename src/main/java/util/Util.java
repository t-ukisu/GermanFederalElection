package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dev6905768cd
 */
public class Util {

    /**
     * @param <K>   Mapのキーとなるクラス
     * @param <V>   Mapの値となるクラス
     * @param key   キー
     * @param value 値
     * @return
     */
    public static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * @param <K>           Mapのキーとなるクラス
     * @param <V>           Mapの値となるクラス
     * @param baseMap       基となるMap
     * @param additionalMap 追加のMap
     * @return
     */
    public static <K, V> Map<K, V> copyMap(Map<K, V> baseMap, Map<K, V> additionalMap) {
        baseMap.putAll(additionalMap);
        return baseMap;
    }

    /**
     * @param <BO>         基のオブジェクトのクラス
     * @param <IO>         基のオブジェクトから作り出されたListのクラス
     * @param <K>          戻り値のMapのキーのクラス
     * @param <V>          戻り値のMapの値のクラス
     * @param baseObject   基のオブジェクト
     * @param listCreator  基のオブジェクトからListを作り出すFunction
     * @param keyCreator   ListのクラスからMapのキーを作り出すFunction
     * @param valueCreator 基のオブジェクトとListのクラスからMapの値を作り出すFunction
     * @return Map
     */
    public static <BO, IO, K, V> Map<K, V> replaceMapKeys(BO baseObject, Function<BO, List<IO>> listCreator, Function<IO, K> keyCreator, BiFunction<BO, IO, V> valueCreator) {
        return listCreator.apply(baseObject).stream()
                .collect(Collectors.toMap(keyCreator, createValue(baseObject, valueCreator)));
    }

    /**
     * @param <BO>
     * @param <IO>
     * @param <K>
     * @param <V>
     * @param keyCreator
     * @param valueCreator
     * @return
     */
    public static <BO, IO, K, V> BiFunction<BO, IO, Map<K, V>> mapCreator(Function<BO, K> keyCreator, Function<IO, V> valueCreator) {
        return (baseObject, innerObject) -> createMap(keyCreator.apply(baseObject), valueCreator.apply(innerObject));
    }

    /**
     * @param <T>
     * @param <K>
     * @param classifier
     * @return
     */
    public static <T, K> Collector<T, ?, Stream<Map.Entry<K, List<T>>>> toGroupedEntry(Function<T, K> classifier) {
        return toGroupedEntry(classifier, Collectors.toList());
    }

    /**
     * @param <T>
     * @param <K>
     * @param <A>
     * @param <D>
     * @param classifier
     * @param downstream
     * @return
     */
    public static <T, K, A, D> Collector<T, ?, Stream<Map.Entry<K, D>>> toGroupedEntry(Function<T, K> classifier, Collector<T, A, D> downstream) {
        return Collectors.collectingAndThen(Collectors.groupingBy(classifier, downstream), map -> map.entrySet().stream());
    }

    /**
     * @param <BO>
     * @param <IO>
     * @param <V>
     * @param baseObject
     * @param valueCreator
     * @return
     */
    private static <BO, IO, V> Function<IO, V> createValue(BO baseObject, BiFunction<BO, IO, V> valueCreator) {
        return innerObject -> valueCreator.apply(baseObject, innerObject);
    }
}
