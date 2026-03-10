package com.lib.cimenginecoreso;

/** 用户词数据
 */
public class JavaUserWord
{
    /** 拼音
     @note: 格式1：wo'men  格式2：wo,men
     */
    public String m_pinyin;

    /** 文本
     */
    public String m_text;

    public JavaUserWord(String pinyin, String text) {
        super();
        m_pinyin = pinyin;
        m_text = text;
    }

    public JavaUserWord() {
    }

}
