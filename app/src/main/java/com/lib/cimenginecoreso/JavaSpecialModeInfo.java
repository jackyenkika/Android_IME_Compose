package com.lib.cimenginecoreso;

/** 特殊模式信息
 */
public class JavaSpecialModeInfo
{
    /** 特殊模式ID
     */
    public int m_mode;

    /** 附加值
     */
    public int m_extraValue;

    /** 显示在右上角的提示文本1
     */
    public String m_hintText;
    
    /** 显示在右上角的提示文本2
     */
    public String m_hintText2;

    public JavaSpecialModeInfo() {
        m_mode = 0;
        m_extraValue = -1;
    }
}
