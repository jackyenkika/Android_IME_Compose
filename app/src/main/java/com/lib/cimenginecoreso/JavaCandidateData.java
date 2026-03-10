package com.lib.cimenginecoreso;

/** 候选数据
 */
public class JavaCandidateData
{
    /** 候选类型
     */
    public int m_type;

    /** 候选文本
     */
    public String m_text;
    
    /** 是否智能纠错
     */
    public boolean m_isIntelligentCorrect;
    
    /** 候选索引
     */
    public int m_index;
    
    /** 候选的来源词库Id
     */
    public int m_srcDictId;
    
    /** 用户词的原始来源词库Id
     */
    public int m_usrWordSrcDictId;
    
    /** 候选的来源词库名称
    */
    public String m_srcDictName;

    
    /** 拼音类型：0 - 都是全拼，1 - 单字简拼，2 - 仅末字简拼（单字不算），3 - 非末字简拼
    */
    public int m_pyType;

    public JavaCandidateData() {
        m_type = CANDIDATE_NULL;
        m_isIntelligentCorrect = false;
        m_index = -1;
        m_srcDictId = -1;
        m_usrWordSrcDictId = -1;
        m_pyType = 0;
    }

    @Override
    public String toString() {
        return m_text + "/" + m_index;
    }

/**------------------------------------------------
                 候选类型定义
-----------------------------------------------------*/
    /** 空候选
     */
    public static final int CANDIDATE_NULL = 0;

    /** 完整词候选
     */
    public static final int CANDIDATE_FULL_WORD = 1;

    /** 部分词候选
     */
    public static final int CANDIDATE_PART_WORD = 2;

    /** 单字候选
     */
    public static final int CANDIDATE_ZI = 3;

    /** 智能组句候选
     */
    public static final int CANDIDATE_SMART_SENTENCE = 4;

    /** 模糊音候选
     */
    public static final int CANDIDATE_FUZZY = 5;

    /** 智能模糊音候选
     */
    public static final int CANDIDATE_INTELLIGENT_FUZZY = 6;

    /** 智能纠错候选
     */
    public static final int CANDIDATE_INTELLIGENT_CORRECTED = 7;

    /** 词语联想候选
     */
    public static final int CANDIDATE_ASSOCIATIVE_WORD = 8;

    /** 云候选
     */
    public static final int CANDIDATE_CLOUD = 9;

    /** iuv大写字母候选
     */
    public static final int CANDIDATE_EXTEND_SYLLABLE = 10;

    /** 英文候选
     */
    public static final int CANDIDATE_ENGLISH = 11;

    /** 超级简拼候选
     */
    public static final int CANDIDATE_SUPER_SIMPLE = 12;

    /** 推荐候选
     */
    public static final int CANDIDATE_RECOMMEND = 13;

    /** 自定义短语候选
     */
    public static final int CANDIDATE_CUSTOM = 14;

    /** 云联想词候选
     */
    public static final int CANDIDATE_CLOUD_LENOVO = 15;
    
    /** Emoji候选
     */
    public static final int CANDIDATE_EMOJI = 16;
        
    /** 云预取联想候选
     */
    public static final int CANDIDATE_CLOUD_ASSOCIATIVE = 17;
    
    /** 中英文混输候选
     */
    public static final int CANDIDATE_MIX = 18;

    /** 城市候选
     */
    public static final int CANDIDATE_CITY = 19;
    
    /** 误读词候选
    */
    public static final int CANDIDATE_MISREAD = 20;
    
    /** 细胞词库候选
    */
    public static final int CANDIDATE_CELL = 21;

    /** 人名词候选
    */
    public static final int CANDIDATE_NAME = 22;

    /** 手写词候选
    */
    public static final int CANDIDATE_HANDWRITE = 23;

    /** 笔画候选
    */
    public static final int CANDIDATE_BIHUA = 24;

    /** 五笔候选
    */
    public static final int CANDIDATE_WUBI = 25;

    /** 通讯录候选
    */
    public static final int CANDIDATE_CONTACT = 26;

    /** 右上角云输入候选（客户端专用，放最后）
     */
    public static final int CANDIDATE_CLOUD_INPUT = 99;

    /** 联系人信息候选（客户端专用，放最后）
     */
    public static final int CANDIDATE_CONTACT_INFO = 100;

/**------------------------------------------------
                 来源词库Id定义
-----------------------------------------------------*/

    /** 用户词库ID
    */
    public static final int USER_WORDLIB_ID = 0;

    /** 系统词库ID
    */
    public static final int CORE_WORDLIB_ID = 1;

    /** 热词词库ID
    */
    public static final int HOT_WORDLIB_ID = 2;

    /** 城市词词库ID
    */
    public static final int CITY_WORDLIB_ID = 3;

    /** 根据云端数据生成候选的来源ID
    */
    public static final int CLOUD_WORDLIB_ID = 4;

    /** 细胞词库ID
    */
    public static final int CELL_WORDLIB_ID = 5;

    /** 人名词库ID
    */
    public static final int NAME_WORDLIB_ID = 6;

    /** 标点联想词库ID
    */
    public static final int PUNC_WORDLIB_ID = 7;

    /** 用户二元词库ID
    */
    public static final int USER_BIGRAM_WORDLIB_ID = 8;

    /** 系统二元词库ID
    */
    public static final int SYS_BIGRAM_WORDLIB_ID = 9;

    /** 通讯录词库ID
    */
    public static final int CONTACT_WORDLIB_ID = 10;

}
