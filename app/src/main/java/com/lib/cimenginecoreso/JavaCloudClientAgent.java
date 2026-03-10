package com.lib.cimenginecoreso;

/** 云输入请求类
 */
public class JavaCloudClientAgent
{
    /**	发起云请求
     @param &in correct 客户端拼音纠错的配置
     @param &in fuzzy 客户端模糊音配置
     @param &in smartFuzzy 客户端智能模糊音配置
     @param &in inputString 用户的原始输入字符串
     @param &in lastWord 上一个上屏的词
     @param &in lastLastWord 上上个上屏的词
     @param &in firstWord 当前的全匹配候选词1
     @param &in secondWord 当前的全匹配候选词2
     @param &in correctString 纠错串，可能是空
     @param &in correctValue 纠错串得分
     @param &in is9KeyMode 是否9键模式
     */
    void RequestCloud(int correct, int fuzzy, boolean smartFuzzy, String inputString, String lastWord, String lastLastWord, String firstWord, String secondWord, String correctString, int correctValue, boolean is9KeyMode){

    }

    /** 取消之前的云请求（即当前输入不需要请求云）
     */
    void CancelRequest(){

    }
}
