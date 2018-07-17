package com.tlz.indexrecyclerview

/**
 * Created by Tomlezen.
 * Data: 2018/7/9.
 * Time: 9:27.
 * @param index 索引, 绘制时会截取第一个字符.
 */
abstract class SampleIndex(index: String) : Index(index){

    /**
     * 当前索引下的数据条数
     * @return Int.
     */
    abstract fun getDataCount(): Int

}