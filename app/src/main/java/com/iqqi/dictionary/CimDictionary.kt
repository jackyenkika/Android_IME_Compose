package com.iqqi.dictionary

import com.iqqi.ime.BuildConfig
import com.iqqi.util.LogObj
import com.lib.cimenginecoreso.CimEngineCore
import com.lib.cimenginecoreso.JavaCandidateData
import com.lib.cimenginecoreso.JavaCloudClientAgent

class CimDictionary : Dictionary {

    private val candidateData: MutableList<JavaCandidateData> = mutableListOf()
    var isInit = false
    var isPredict = false

    var nowMode = -1

    private var pinyinSelectedContent: MutableList<String> = mutableListOf()

    fun free() {
        candidateData.clear()
        CimEngineCore.CoreUnInit()
        pinyinSelectedContent.clear()
        isInit = false
    }

    fun setT9() {
        nowMode = 1
        LogObj.trace("nowMode = $nowMode")
        CimEngineCore.CoreSwitchKeyboardMode(1)
    }

    fun setQwerty() {
        nowMode = 0
        LogObj.trace("nowMode = $nowMode")
        CimEngineCore.CoreSwitchKeyboardMode(0)
    }

    //筆畫
    fun setSK() {
        CimEngineCore.CoreSwitchKeyboardMode(3)
    }

    override fun init(userDataDict: String): Int {
        val cloudAgent = JavaCloudClientAgent()
        CimEngineCore.CoreSetAppName(BuildConfig.APPLICATION_ID)

        val isQWERTY = if (nowMode < 0) 0 else nowMode
        val coreInitStatus = CimEngineCore.CoreInit3(
            cloudAgent,
            isQWERTY,
            true,
            0,
            "",
            BuildConfig.APPLICATION_ID,
            userDataDict
        )
        if (!coreInitStatus) {
            return CimEngineCore.CoreGetLastError()
        }
        isInit = true
        return 0
    }


    fun getCandidateCount(): Int {
        if (!isInit) return 0
        return candidateData.size
    }

    override fun query(code: String): List<String> {
        if (!isInit) return emptyList()

        resetCore()
        CimEngineCore.CoreProcessString(code)

        isPredict = false

        loadCandidates(0, 50)

        val list = mutableListOf<String>()
        candidateData.forEach {
            list.add(it.m_text)
        }
        return list
    }


    fun getCandidateData(): MutableList<JavaCandidateData> {
        return candidateData
    }


    override fun predict(previous: String): List<String> {
        if (candidateData.isEmpty()) {
            return emptyList()
        }
        var selectedIndex = 0

        while (selectedIndex < candidateData.size) {
            if (candidateData[selectedIndex].m_text == previous) {
                CimEngineCore.CoreSelectCandidate(selectedIndex)
                break
            }
            selectedIndex++
        }
        if (getEngineUnitSuccess()) {
            CimEngineCore.CoreMakeRecommendCandidates(previous)
            isPredict = true
        }
        loadCandidates(0, 50)

        val list = mutableListOf<String>()
        candidateData.forEach {
            list.add(it.m_text)
        }
        return list
    }

    fun getEngineUnitSuccess(): Boolean {
        if (!isInit) return true
        return CimEngineCore.CoreCanSendInputResult()
    }

    fun resetCore() {
        if (!isInit) return
        CimEngineCore.CoreReset()
        if (nowMode == 1) {
            setT9()
        }
//        LogObj.trace("resetCore", 4, true)
    }

    private fun loadCandidates(begin: Int, number: Int): Int {
        candidateData.clear()

        val raw = CimEngineCore.CoreGetCandidateArray(begin, number) ?: return 0

        raw.take(number - begin)             // 控制回傳筆數
            .forEach { candidateData.add(it) }

        return raw.size
    }

    override fun composing(compose: String): String {
        val composing = try {
            CimEngineCore.CoreGetComposeString() ?: ""
        } catch (e: Exception) {
            LogObj.trace("getComposeText got error", e)
            compose
        }
        return composing
    }

    fun getPinyinArray(): List<String> {
        val raw = CimEngineCore.CoreGetPinyinArray()
        return if (raw.isEmpty()) emptyList() else raw.toList()
    }

    /**
     * 選擇拼音
     */
    fun selectPinyin(index: Int, content: String) {
        pinyinSelectedContent.add(content)
        CimEngineCore.CoreSelectPinyin(index)

        val candidateCount = CimEngineCore.CoreGetCandidateCount()
        if (candidateCount > 0) {
            loadCandidates(0, 50)
        }
    }

    /**
     * 獲取選擇過的拼音列表
     */
    fun getPinyinSelectedContent(): List<String> {
        return pinyinSelectedContent
    }

    /**
     * 清空已選擇的拼音列表，避免重複問題
     */
    fun resetPinyinSelectedContent() {
        pinyinSelectedContent.clear()
    }

}