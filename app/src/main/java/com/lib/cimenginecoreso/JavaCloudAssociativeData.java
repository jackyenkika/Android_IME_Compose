package com.lib.cimenginecoreso;

/**
 * 云预取联想结果
 */
public class JavaCloudAssociativeData {
    /**
     * 上文(汉字)
     */
    public String m_precedingText;

    /**
     * 云预取联想结果数组
     */
    public JavaCloudAssociativeItem[] m_itemArr;

    public JavaCloudAssociativeData(String precedingText, JavaCloudAssociativeItem[] itemArr) {
        super();
        m_precedingText = precedingText;
        m_itemArr = itemArr;
    }

    public JavaCloudAssociativeData() {
    }
}
