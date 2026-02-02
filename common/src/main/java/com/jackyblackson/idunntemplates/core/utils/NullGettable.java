package com.jackyblackson.idunntemplates.core.utils;

/**
 * 允许对象提供自身的"空对象"实现。
 * 用于在不允许存储 null 的集合中占位。
 *
 * @param <T> 实现类的类型
 */
public interface NullGettable<T> {
    /**
     * 获取代表 "Null" 状态的实例
     * @return 一个非 null 的对象，代表业务上的空值
     */
    T getNullInstance();
}
