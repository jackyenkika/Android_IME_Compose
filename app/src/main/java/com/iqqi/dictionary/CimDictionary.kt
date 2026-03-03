package com.iqqi.dictionary

class CimDictionary : Dictionary {

    override fun query(code: String): List<String> {
        // 查表、查 trie、查 DB
        return listOf("你", "尼", "呢")
    }

    override fun predict(previous: String): List<String> {
        TODO("Not yet implemented")
    }
}