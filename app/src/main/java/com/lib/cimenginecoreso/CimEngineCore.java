package com.lib.cimenginecoreso;

public class CimEngineCore {

    // Used to load the 'PinyinCore' library on application startup.
    static {
        System.loadLibrary("CimEngineCore");
    }

    // 成功
    public static final int CORE_ERR_SUCCESS = 0;

    // 内存申请错误
    public static final int CORE_ERR_ALLOC_MEMORY = 1;

    // 加载拼音切分数据错误
    public static final int CORE_ERR_LOAD_PINYIN = 2;

    // 加载字库错误
    public static final int CORE_ERR_LOAD_ZILIB = 3;

    // 加载系统词库错误
    public static final int CORE_ERR_LOAD_SYS_WORDLIB = 4;

    // 加载语言模型错误
    public static final int CORE_ERR_LOAD_SYSMODEL = 5;

    // 初始化输入会话错误
    public static final int CORE_ERR_INIT_SESSION = 6;

    // 内核未初始化错误
    public static final int CORE_ERR_NOT_INIT = 7;

    // 加载热词词库错误
    public static final int CORE_ERR_LOAD_HOT_WORDLIB = 8;

    // 加载用户词库错误
    public static final int CORE_ERR_LOAD_USER_WORDLIB = 9;

    // 初始化成功，但是对系统词库进行文件映射失败
    public static final int CORE_ERR_MMAP_SYS_WORDLIB = 10;

    // 初始化成功，但是重复调用了初始化
    public static final int CORE_ERR_REPEAT_INIT = 11;

    // 初始化成功，但是检查拷贝目录的词库失败
    public static final int CORE_ERR_CHECK_COPY_PATH = 12;

    // 加载Emoji词库错误
    public static final int CORE_ERR_LOAD_EMOJI_DICT = 13;

    // 加载删词词库错误
    public static final int CORE_ERR_LOAD_BAN_DICT = 14;

    // 加载城市词库错误
    public static final int CORE_ERR_LOAD_CITY_DICT = 15;

    // 加载个性化推荐词库错误
    public static final int CORE_ERR_LOAD_CELL_DICT = 16;
    //老维语
    public static final int OLD_UIGHUR = 0;
    //新维语
    public static final int NEW_UIGHUR = 1;

    /**
     * 内核初始化（26键）
     *
     * @param &in cloudAgent 云输入代理请求类
     * @return 成功true，失败false
     */
    public static native boolean CoreInit(JavaCloudClientAgent cloudAgent);

    /**
     * 内核初始化
     *
     * @param &in cloudAgent 云输入代理请求类
     * @param &in keyboardMode 0：26key，1：9key，2：手写，3：笔画
     * @return 成功true，失败false
     */
    public static native boolean CoreInit2(JavaCloudClientAgent cloudAgent, int keyboardMode);

    /**
     * 内核初始化
     *
     * @param &in cloudAgent 云输入代理请求类
     * @param &in keyboardMode 0：26key，1：9key，2：手写，3：笔画
     * @param &in isEnableIntelligentCorrect 是否开启智能纠错
     * @param &in fuzzyOption 模糊音选项
     * @param &in copyDictPath 安装应用时拷贝词库的路径，要带"/"结尾
     * @param &in productName 产品英文名称，如"kikakeyboard"
     * @param &in userDataPath 用户数据的路径，要带"/"结尾
     * @return 成功true，失败false
     */
    public static native boolean CoreInit3(JavaCloudClientAgent cloudAgent, int keyboardMode, boolean isEnableIntelligentCorrect, int fuzzyOption, String copyDictPath, String productName, String userDataPath);

    /**
     * 内核反初始化
     */
    public static native void CoreUnInit();

    /**
     * 设置当前App的包名
     *
     * @param &in appName 当前App的包名，如"com.tencent.mm"
     */
    public static native void CoreSetAppName(String appName);

    /**
     * 内核强制保存词库
     */
    public static native void CoreSaveDict();

    /**
     * 设置内核参数
     *
     * @param &in isEnableIntelligentCorrect 是否开启智能纠错
     * @param &in fuzzyOption 模糊音选项
     * @param &in wbAddPinyinCands 是否在五笔模式下添加拼音候选
     * @param &in wbShowWubiCodes 是否在五笔模式下显示拼音候选的五笔码
     * @param &in bHwEmoji 是否开启手写emoji表情识别，默认开启
     * @param &in isEnableContactInfo 是否开启联系人信息，默认开启
     */
    public static native void CoreSetOptions(boolean isEnableIntelligentCorrect, int fuzzyOption, boolean wbAddPinyinCands, boolean wbShowWubiCodes, boolean bHwEmoji, boolean isEnableContactInfo);

    /**
     * 向内核发送一个按键
     *
     * @param &in key 按键的ascii码
     * @param &in xpos 按键的x坐标
     * @param &in ypos 按键的y坐标
     * @return true:该按键已被内核处理, false:该按键没有被内核处理
     */
    public static native boolean CoreProcessKey(int key, int xpos, int ypos);

    /**
     * 构建键盘布局
     *
     * @param &in w 键盘的宽度
     * @param &in h 键盘的高度
     * @param &in pixels 手机像素，分为两种1080和720
     * @param &in keyboardMode 键盘类型，9键、26键
     */
    public static native void CoreBuildLayout(int w, int y, int pixels, int keyboardMode);

    /**
     * 处理一串拼音
     *
     * @param &in pinyinStr 拼音串（不能包含非拼音字符）
     * @return 实际处理的拼音字符个数
     */
    public static native int CoreProcessString(String pinyinStr);

    /**
     * 切换中英文模式
     *
     * @param &in isCnMode 传true切换到中文模式，传false切换到纯英文模式
     */
    public static native void CoreSwitchCnEnMode(boolean isCnMode);

    /**
     * 切换简繁体模式
     *
     * @param &in isTradMode 传true切换到繁体模式，传false切换到简体模式
     */
    public static native void CoreSwitchSimpTradMode(boolean isTradMode);

    /**
     * 在普通模式和特殊模式之间切换
     *
     * @param &in info 传非null时切换到特殊模式，传null时返回到普通模式
     */
    public static native void CoreSwitchSpecialMode(JavaSpecialModeInfo info);

    /**
     * 获取候选项总数
     *
     * @return 候选项数量
     */
    public static native int CoreGetCandidateCount();

    /**
     * 选择一个候选
     *
     * @param &in index 候选索引
     */
    public static native void CoreSelectCandidate(int index);

    /**
     * 获取候选详情
     *
     * @param &in index 候选索引
     * @return 候选详情
     */
    public static native String CoreGetCandidateDetail(int index);

    /**
     * 修改焦点候选
     *
     * @param &in index 候选索引
     * @return 用于显示的拼音串（已切分）
     */
    public static native String CoreChangeFocusCandidate(int index);

    /**
     * 判断是否可以发送上屏结果给当前App
     *
     * @return 可以true，不可以false
     */
    public static native boolean CoreCanSendInputResult();

    /**
     * 获取结果文本
     *
     * @return 结果文本
     */
    public static native String CoreGetInputResult();

    /**
     * 获取结果文本，并生成推荐候选
     *
     * @param &in textStr 光标前文本，最长10个字符
     * @return 结果文本
     */
    public static native String CoreGetInputResultAndMakeRecommendCandidates(String textStr);

    /**
     * 重置内核状态
     *
     * @note: 在上屏候选 或 清空当前输入后调用
     */
    public static native void CoreReset();

    /**
     * 清空内核上下文
     *
     * @note: 在在非编辑状态时按下backspace、发送、确定、标点符号，以及编辑状态按回车（上屏输入字母串）后调用
     */
    public static native void CoreClearContext();

    /**
     * 获取用户的原始输入串
     *
     * @return 输入串
     */
    public static native String CoreGetInputString();

    /**
     * lstm模型计算
     */
    public static native void CoreLstmForward();

    /**
     * 获取用于显示的拼音串（已切分）
     *
     * @return 拼音串
     */
    public static native String CoreGetComposeString();

    /**
     * 获取纠错区间数组
     *
     * @return 纠错区间数组
     */
    public static native JavaPinyinRange[] CoreGetCorrectRangeArr();

    /**
     * 获取最近一次删除输入串的字符
     *
     * @return 最近一次删除输入串的字符，没有则为0
     */
    public static native int CoreGetLastDeleteCh();

    /**
     * 获取一个候选数据
     *
     * @param &in index 候选索引
     * @return 候选数据
     */
    public static native JavaCandidateData CoreGetCandidateData(int index);

    /**
     * 获取一个候选数据
     *
     * @param &in index 候选起始索引
     * @param &in maxCount 最大候选数量
     * @return 候选数据数组
     */
    public static native JavaCandidateData[] CoreGetCandidateArray(int index, int maxCount);

    /**
     * 获取获取单字候选的起始索引
     *
     * @return 起始索引
     */
    public static native int CoreGetZiCandidateBeginIndex();

    /**
     * 设置云返回结果到内核
     *
     * @param &in resultArr 云输入结果数组
     * @return true:成功, false:失败
     */
    public static native boolean CoreSetCloudResponse(JavaCloudResponseItem[] resultArr);

    /**
     * 设置云预取联想结果到缓存
     *
     * @param &in resultArr 云预取联想响应结果
     * @return true:成功, false:失败
     */
    public static native boolean CoreSetCloudAssociativeResults(JavaCloudAssociativeData[] resultArr);

    /**
     * 获取词库下载文件夹
     *
     * @return 词库下载文件夹，包含"/"结尾
     */
    public static native String CoreGetDictDownloadFolder();

    /**
     * 获取用户数据文件夹
     *
     * @return 用户数据文件夹，包含"/"结尾
     */
    public static native String CoreGetUserDataFolder();

    /**
     * 加密数据（不用调Core_Init初始化）
     *
     * @param &in srcBuffer 缓冲区
     * @param &in bufferSize 缓冲区大小
     * @param &in destBuffer 目标缓冲区
     * @return true 成功，false 失败
     */
    public static native boolean CoreEncryptData(byte[] srcBuffer, int bufferSize, byte[] destBuffer);

    /**
     * 解密数据（不用调Core_Init初始化）
     *
     * @param &in srcBuffer 缓冲区
     * @param &in bufferSize 缓冲区大小
     * @param &in destBuffer 目标缓冲区
     * @return true 成功，false 失败
     */
    public static native boolean CoreDecryptData(byte[] srcBuffer, int bufferSize, byte[] destBuffer);

    /**
     * 重新加载热词词库（必须在当前没有用户输入的情况下调用）
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreReloadHotWordLib();

    /**
     * 重新加载Emoji词库（必须在当前没有用户输入的情况下调用）
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreReloadEmojiDict();

    /**
     * 重新加载删词词库（必须在当前没有用户输入的情况下调用）
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreReloadBanDict();

    /**
     * 重新加载城市词库（必须在当前没有用户输入的情况下调用）
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreReloadCityDict();

    /**
     * 重新加载下载目录的个性推荐词库（必须在当前没有用户输入的情况下调用）
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreReloadCellDict();

    /** 释放下载目录下的指定细胞词库
     @param &in cellType 细胞词库类型，-1: 表示释放所有细胞词库，0: 表示释放地图类细胞词库，1：表示释放多媒体类细胞词库
     @return true：成功，false：失败
     */
    public static native boolean CoreFreeCellDict(int cellType);

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public static native int CoreGetLastError();

    public static native String CoreGetLastErrorString();

    /**
     * 把简体文本转换成繁体
     *
     * @param &in text 简体文本
     * @return 繁体文本
     */
    public static native String CoreConvertSimpToTrad(String text);

    /**
     * 把输入文本转换成emoji
     *
     * @param &in text 输入文本
     * @return 索引0: utf-16编码的emoji文本  索引1: 带分割信息的输入文本
     */
    public static native String[] CoreConvertTextToEmoji(String text);

    /**
     * 把输入emoji转换成文本
     *
     * @param &in utf-16编码的emoji文本
     * @return 索引0: 转换后文本  索引1: 带分割信息的emoji文本
     */
    public static native String[] CoreConvertEmojiToText(String emoji);

    /**
     * 保存用户词
     *
     * @param &in py 拼音（必须都是全拼）
     * @param &in py 汉字串
     */
    public static native void CoreSaveUserWord(String py, String hz);

    /**
     * 导入通讯录词库
     *
     * @param &in textArr 通讯录人名数组
     * @return 导入的条数
     */
    public static native int CoreImportContacts(String[] textArr);

    /**
     * 清除通讯录词库
     *
     * @return true 成功，false 失败
     */
    public static native boolean CoreClearContacts();

    /**
     * 获取内核上文记录的用户上次上屏的通讯录候选文本
     *
     * @return 通讯录候选文本
     */
    public static native String CoreGetContextContactText();

    /**
     * 获取内核函数调用堆栈字符串
     *
     * @return 内核函数调用堆栈字符串
     */
    public static native String CoreGetCallStackStr();

    /**
     * 获取个性化推荐词库的配置信息
     *
     * @return 个性化推荐词库的配置信息
     */
    public static native JavaCellConfigInfo[] CoreGetCellConfig();

    /**
     * 设置个性化推荐词库的配置信息
     *
     * @param &in infoArr 个性化推荐词库的配置信息
     * @return true 成功，false 失败
     */
    public static native boolean CoreSetCellConfig(JavaCellConfigInfo[] infoArr);

    /**
     * 获取特殊模式的信息
     *
     * @return 特殊模式的信息
     */
    public static native JavaSpecialModeInfo CoreGetSpecialModeInfo();

    /**
     * 客户端收起更多候选窗口时的回调
     */
    public static native void CoreOnCollapse();

    /**
     * 客户端下拉展开更多候选窗口时的回调
     */
    public static native void CoreOnExpand();

    /**
     * 切换键盘模式
     *
     * @param &in keyboardMode 0：26key，1：9key，2：手写，3：笔画
     */
    public static native void CoreSwitchKeyboardMode(int keyboardMode);

    /**
     * 获取当前的拼音列表（9键模式）
     *
     * @return 拼音列表
     */
    public static native String[] CoreGetPinyinArray();

    /**
     * 选择一个拼音（9键模式）
     *
     * @param &in index 拼音索引
     */
    public static native void CoreSelectPinyin(int index);

    /**
     * 获取已选择拼音串的区间，用于绘制（9键模式）
     *
     * @return 拼音区间
     */
    public static native JavaPinyinRange CoreGetSelectedPinyinRange();

    /**
     * 在每次输入周期的首次按键时，将光标前内容传给内核，9/26键中文模式下生效
     *
     * @param &in textStr 光标前文本，最长10个字符
     */
    public static native void CoreSetTextBeforeCursor(String textStr);

    /**
     * 设置是否启用高性能消耗的功能
     *
     * @param &in isEnable 传true启用，传false关闭
     */
    public static native void CoreSetEnableHighCostFunc(boolean isEnable);

    /*------------------------------------------
     *             手写联想模式
     *------------------------------------------*/

    /**
     * 根据上屏文本，生成推荐候选
     *
     * @param &in textStr 上屏文本
     */
    public static native void CoreMakeRecommendCandidates(String textStr);

    /**
     * 根据手写笔迹，识别单字
     *
     * @param &in tracks 笔迹坐标数组
     * @param &in width 手写窗口宽度
     * @param &in height 手写窗口高度
     * @param &in candNum 请求返回单字个数
     * @param &in option 掩码参数，目前暂未使用
     * @return 结果单字数组
     */
    public static native char[] CoreHandWriteRecognizeWR(short[] tracks, short width, short height, int candNum, int option);

    /**
     * 根据手写笔迹，识别单字
     *
     * @param &in tracks 笔迹坐标数组
     * @param &in width 手写窗口宽度
     * @param &in height 手写窗口高度
     * @param &in candNum 请求返回单字个数
     * @param &in option 掩码参数，目前暂未使用
     */
    public static native void CoreHandWriteRecognize(short[] tracks, short width, short height, int candNum, int option);

    /**
     * 性能测试，客户端不调用
     */
    public static native void CoreRunPerformance();

    public static native void CoreRunAndroidPerformance();

    public static native int CoreGetRandomKey();

    public static native void CoreRemoveUserData();

    /**
     * 设置产品英文名称，用来区分不同产品的用户数据路径
     *
     * @param &in productName 产品英文名称，如"kikakeyboard"
     */
    public static native void CoreSetProductName(String productName);

    public static native void SetShowEmoji(boolean isShowEmoji);

    /**
     * 获取引擎版本号，内部使用
     */
    public static native String CoreGetEngineVersion();
}
