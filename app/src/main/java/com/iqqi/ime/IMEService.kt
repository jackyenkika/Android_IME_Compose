package com.iqqi.ime

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.iqqi.engine.IMEEngine
import com.iqqi.engine.IMEReducer
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

    private lateinit var engine: IMEEngine
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
            settingRepository = container.settingsRepository,
            languageRepository = container.languageRepository,
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
        val allLanguages = container.languageRepository.getAvailableLanguages()
        val lang = allLanguages.firstOrNull { it.locale == lastLocale } ?: allLanguages.first()

        createEngine(lang)
        IMEStore.updateKeyboardState(
            IMEStore.keyboardState.value.copy(
                language = lang,
                inputType = attributes.inputType,
                showLanguageMenu = false,
                animationShakeTick = 0,
                animationTick = false,
            )
        )

        // 3️⃣ 清掉貼圖面板可見性
        IMEStore.updateStickerState(false)
    }

    override fun onKeyDown(code: Int, event: KeyEvent): Boolean {
        val action = mapper.map(event) ?: return super.onKeyDown(code, event)

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

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )

        val ic = currentInputConnection ?: return

        val isSelectionChanged = oldSelStart != newSelStart || oldSelEnd != newSelEnd

        val hasComposing = candidatesStart != -1 && candidatesEnd != -1

        var shouldClear = false

        // ------------------------------------------------
        // 1️⃣ 光標移出 composing 區域
        // ------------------------------------------------
        if (hasComposing) {
            if (newSelStart !in candidatesStart..candidatesEnd ||
                newSelEnd !in candidatesStart..candidatesEnd
            ) {
                shouldClear = true
            }
        }

        // ------------------------------------------------
        // 2️⃣ 沒有 composing，但光標被移動（使用者點擊）
        // 👉 很多 App 不會給 candidates range
        // ------------------------------------------------
        if (!hasComposing && isSelectionChanged) {
            shouldClear = true
        }

        // ------------------------------------------------
        // 3️⃣ 執行清理（最關鍵）
        // ------------------------------------------------
        if (shouldClear) {
            LogObj.trace("⚠️ Force finish composing due to cursor move")

            ic.finishComposingText()

            // 🔥 關鍵：同步清 Engine 狀態
            dispatch(ImeAction.Reset)   // ← 你需要這個 action

            IMEStore.clearCandidate()
        }
    }
    //=========================================================

    fun dispatch(action: ImeAction) {
        inputDispatcher.dispatch(action)
    }

    fun onDeleteKeyDown() {
        val ic = currentInputConnection ?: return

        // 先判斷 EngineState buffer
        val bufferNotEmpty =
            engine.currentState.buffer.isNotEmpty() || engine.currentState.composing.isNotEmpty() || engine.currentState.candidates.isNotEmpty() || engine.currentState.predictingCandidates.isNotEmpty()

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

        engine = IMEEngine(
            reducer = IMEReducer(this@IMEService, language = language.name, dictionary)
        )

        inputDispatcher = InputDispatcher(
            engine = engine, language = language.name, renderer = imeRender
        )
    }

    fun switchLanguage(lang: ImeLanguage) {

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val imi =
            imm.enabledInputMethodList.firstOrNull { it.packageName == packageName } ?: return

        lang.subtype?.let {
            window?.window?.attributes?.token?.let { token ->
                imm.setInputMethodAndSubtype(
                    token, imi.id, it
                )
            }
        }

        // ⭐ 1. 重建 Engine（核心）
        createEngine(lang)

        // ⭐ 2. 清候選字
        IMEStore.clearCandidate()

        // 更新 keyboardState
        val newState = IMEStore.keyboardState.value.copy(
            language = lang, showLanguageMenu = false
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
            this, "${packageName}.stickerprovider", stickerFile
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
}