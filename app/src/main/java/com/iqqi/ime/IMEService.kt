package com.iqqi.ime

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.iqqi.ImeApplication
import com.iqqi.core.ImeAction
import com.iqqi.dictionary.CimDictionary
import com.iqqi.dictionary.Dictionary
import com.iqqi.dictionary.KikaDictionary
import com.iqqi.engine.CIMReducer
import com.iqqi.engine.ImeEngine
import com.iqqi.ime.util.DeleteRepeater
import com.iqqi.ime.util.LogObj
import com.iqqi.keyboard.ComposeKeyboardView
import com.iqqi.keyboard.model.ImeLanguage
import com.iqqi.keyboard.model.KeyboardLanguage
import com.iqqi.keyboard.model.Sticker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File

class IMEService : LifecycleInputMethodService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var engine: ImeEngine
    private lateinit var mapper: IMEKeyMapper
    private lateinit var imeRender: IMERenderer

    private val deleteRepeater = DeleteRepeater()

    private lateinit var inputDispatcher: InputDispatcher

    private val dictionaryCache = mutableMapOf<KeyboardLanguage, Dictionary>()
    //ViewModelStore Methods

    private val container
        get() = (application as ImeApplication).container
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle
    override val viewModelStore: ViewModelStore
        get() = store

    //SaveStateRegestry Methods

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        mapper = IMEKeyMapper()
        imeRender = IMERenderer(this@IMEService)
    }

    override fun onCreateInputView(): View {

        val view = ComposeKeyboardView(
            context = this,
            repository = container.settingsRepository,
            stickerRepository = container.stickerRepository
        )

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return view
    }


    override fun onStartInput(attributes: EditorInfo, restarting: Boolean) {
        super.onStartInput(attributes, restarting)

        //清除候選字
        IMEStore.clearCandidate()

        val lastLocale = runBlocking { container.settingsRepository.lastUsedLanguageFlow.first() }
        val allLanguages = getAvailableLanguages(this)
        val lang = allLanguages.firstOrNull { it.locale == lastLocale } ?: allLanguages.first()

        createEngine(lang)
        IMEStore.updateKeyboardState(
            IMEStore.keyboardState.value.copy(
                language = lang,
                inputType = attributes.inputType,
                animationShakeTick = 0,
                animationTick = false,
            )
        )

        // 3️⃣ 清掉貼圖面板可見性
        IMEStore.updateStickerState(false)
    }

    override fun onKeyDown(code: Int, event: KeyEvent): Boolean {
        val action = mapper.map(event)
            ?: return super.onKeyDown(code, event)

        if (action is ImeAction.Delete) {
            onDeleteKeyDown()  // 進入判斷 buffer 或長按刪除
            return true
        }

        dispatch(action)

        return true
    }

    override fun onKeyUp(code: Int, event: KeyEvent): Boolean {
        if (code == KeyEvent.KEYCODE_DEL) {
            onDeleteKeyUp()  // 停止 DeleteRepeater
            return true
        }
        return super.onKeyUp(code, event)
    }
    //=========================================================

    fun dispatch(action: ImeAction) {
        inputDispatcher.dispatch(action)
    }

    fun onDeleteKeyDown() {
        val ic = currentInputConnection ?: return

        // 先判斷 EngineState buffer
        val bufferNotEmpty = engine.currentState.buffer.isNotEmpty() ||
                engine.currentState.composing.isNotEmpty() ||
                engine.currentState.candidates.isNotEmpty() ||
                engine.currentState.predictingCandidates.isNotEmpty()

        if (bufferNotEmpty) {
            // buffer 還有內容 → 走 Engine 刪除
            dispatch(ImeAction.Delete)
        } else {
            // buffer 沒有 → 走長按刪除
            deleteRepeater.singleDelete(ic)
            deleteRepeater.start(ic)
        }
    }

    fun onDeleteKeyUp() {
        deleteRepeater.stop()
    }

    //=========================================================

    private fun getDictionary(language: KeyboardLanguage): Dictionary {

        return dictionaryCache.getOrPut(language) {
            when (language) {
                KeyboardLanguage.CHINESE -> CimDictionary()
                KeyboardLanguage.ENGLISH -> KikaDictionary(engineId = 1)
            }
        }
    }

    private fun createEngine(language: ImeLanguage) {

        val dictionary = getDictionary(language.name)

        engine = ImeEngine(
            reducer = CIMReducer(this@IMEService, dictionary)
        )

        inputDispatcher = InputDispatcher(
            engine = engine,
            renderer = imeRender
        )
    }

    fun switchLanguage(lang: ImeLanguage) {

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val imi = imm.enabledInputMethodList
            .firstOrNull { it.packageName == packageName }
            ?: return

        lang.subtype?.let {
            window?.window?.attributes?.token?.let { token ->
                imm.setInputMethodAndSubtype(
                    token,
                    imi.id,
                    it
                )
            }
        }

        // ⭐ 1. 重建 Engine（核心）
        createEngine(lang)

        // ⭐ 2. 清候選字
        IMEStore.clearCandidate()

        // 更新 keyboardState
        val newState = IMEStore.keyboardState.value.copy(
            language = lang,
            showLanguageMenu = false
        )

        IMEStore.updateKeyboardState(newState)
        runBlocking {
            container.settingsRepository.setLastUsedLanguage(lang.locale ?: lang.name.name)
        }
    }

    //=========================================================


    fun canCommitSticker(): Boolean {

        val info = currentInputEditorInfo ?: return false

        val mimeTypes = EditorInfoCompat.getContentMimeTypes(info)

        LogObj.trace("mimeTypes: ${mimeTypes.joinToString()}")
        return mimeTypes.any { it.startsWith("image/") }
    }

    fun commitSticker(sticker: Sticker) {
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo ?: return

        // 🔥 取得 Application 已複製的檔案
        val app = application as ImeApplication
        val stickerFile = File(app.filesDir, "stickers/${sticker.id}")

        if (!stickerFile.exists()) {
            LogObj.trace("Sticker file not found: ${stickerFile.absolutePath}")
            return
        }

        // 🔥 生成 content:// URI
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.stickerprovider",
            stickerFile
        )

        val contentInfo = androidx.core.view.inputmethod.InputContentInfoCompat(
            contentUri,
            android.content.ClipDescription("sticker", arrayOf(sticker.mimeType)),
            null
        )

        // 🔥 commitContent 加授權 flag
        androidx.core.view.inputmethod.InputConnectionCompat.commitContent(
            ic,
            info,
            contentInfo,
            androidx.core.view.inputmethod.InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
            null
        )
    }
    //=========================================================

    companion object {
        fun getAvailableLanguages(context: Context): List<ImeLanguage> {
            val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val imi =
                imm.enabledInputMethodList.firstOrNull { it.packageName == context.packageName }
                    ?: return emptyList()

            val subtypes = mutableListOf<InputMethodSubtype>()
            imm.inputMethodList.forEach { imiInfo ->
                if (imiInfo.packageName == context.packageName) {
                    val count = imiInfo.subtypeCount
                    for (i in 0 until count) {
                        subtypes.add(imiInfo.getSubtypeAt(i))
                    }
                }
            }

            return subtypes.map { subtype ->
                val locale = subtype.locale
                val language = when {
                    locale.startsWith("zh") -> KeyboardLanguage.CHINESE
                    else -> KeyboardLanguage.ENGLISH
                }
                ImeLanguage(
                    name = language,
                    locale = locale,
                    subtype = subtype,
                    enabled = imm.getEnabledInputMethodSubtypeList(imi, true)
                        .any { it.locale == locale }
                )
            }
        }
    }
    //=========================================================
}