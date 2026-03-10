package com.lib.cimenginecoreso;

/** 云预取联想结果Item
 */
public class JavaCloudAssociativeItem
{
    /** 服务器返回的查询结果(汉字)
     */
    public String hz;
    
    /** 全拼切分结果
     */
    public String py;
    
    public JavaCloudAssociativeItem(String text, String pinyin)
    {
        super();
        hz = text;
        py = pinyin;
    }

    public JavaCloudAssociativeItem()
    {
    }
}
