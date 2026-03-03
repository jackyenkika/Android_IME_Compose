# Android_IME_4.0

Jack

## 📌 專案簡介

藉由AI重構現行的IME內容

## 🧱 專案架構

- 架構模式：MVVM / MVI / Clean Architecture
- 使用技術：
    - Kotlin
    - Jetpack (ViewModel, LiveData, Room...)
    - Hilt
    - Retrofit

## 📂 專案目錄結構

```text
android/      ← Adapter 層
core/         ← Engine 契約層
dictionary/   ← Domain Service
engine/       ← 實作某一種輸入法
learning/     ← 未來擴充
ui/           ← 純 render
```

## 核心原則（請記住這 4 句）
	1.	core 裡面不能有任何語言知識
	2.	engine 才知道「拼音 / 注音 / 形碼」
	3.	dictionary 只回答「查詢」，不保留狀態
	4.	UI 與 android 永遠不碰 reducer

### 1️⃣ core/ ——「憲法層」，只能放抽象
ImeAction.kt: 只描述「發生什麼」，不描述「怎麼做」
❌ 不要放：
- 拼音
- language
- qwerty / t9
- keyCode = Int

Key.kt（要「小」，而且不可逆）
❌ 不要出現：
- Android KeyEvent
- Int keyCode
- Shift / Ctrl（這是 UI 的事）

EngineState.kt（所有狀態都要「看得到」）
