package com.mrlu.hybrid;

/**
 * Created by : mr.lu
 * Created at : 2019-05-27 at 13:53
 * Description:
 */
public interface IManager<K,T> {

    IManager add(K key,T target);

    T get(K key);
}
