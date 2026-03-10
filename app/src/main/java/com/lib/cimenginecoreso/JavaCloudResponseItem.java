package com.lib.cimenginecoreso;

/** 云输入结果
 */
public class JavaCloudResponseItem
{
    /** 该结果的索引
     */
    public int m_index;

    /** 用户原始输入串
     */
    public String m_inputStr;

    /** 全拼切分结果
     */
    public String m_fullPinyin;

    /** 候选中文
     */
    public String m_resultText;

    public JavaCloudResponseItem(int index, String inputStr, String fullPinyin, String resultText)
    {
        super();
        m_index = index;
        m_inputStr = inputStr;
        m_fullPinyin = fullPinyin;
        m_resultText = resultText;
    }

    public JavaCloudResponseItem()
    {
        m_index = 99;
    }
}
