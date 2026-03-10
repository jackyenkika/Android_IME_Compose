package com.lib.cimenginecoreso;

/** 拼音区间
 */
public class JavaPinyinRange
{
    /** 起始位置
     */
    public int m_beginPos;

    /** 长度
     */
    public int m_length;

    public JavaPinyinRange(int beginPos, int length) {
        super();
        m_beginPos = beginPos;
        m_length = length;
    }

    public JavaPinyinRange() {
        m_beginPos = 0;
        m_length = 0;
    }
}
