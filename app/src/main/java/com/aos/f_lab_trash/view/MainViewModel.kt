package com.aos.f_lab_trash.view

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.f_lab_trash.view.ui.theme.Category
import com.aos.f_lab_trash.view.ui.theme.Item
import com.aos.f_lab_trash.view.ui.theme.Type
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class MainViewModel: ViewModel() {

    private var _items = mutableStateListOf<Item>()
    val items: List<Item> get() = _items
    private var _dumpItems = mutableStateListOf<Item>()
    val dumpItems: List<Item> get() = _dumpItems
    private val countDownJobs = mutableStateMapOf<Int, Job>()

    companion object {
        const val TIME_OUT = 3
    }

    init {
        for(i in 1 .. 50) {
            _items.add(
                Item(
                    id = i,
                    name = "item $i"
                )
            )
        }
    }

    fun onClickedDumpItem(item: Item) {
        if(!item.isActive) {
            startCountDown(item, category = Category.Dump, handleCountdownFinished = {
                _items.removeAll { it.id == item.id }
                _dumpItems.add(item)

                sortAllList()
            })
        }
    }

    fun onClickedRecoveryItem(item: Item) {
        if (!dumpItems.any { it.id == item.id }) {
            Log.e("onClickedRecoveryItem", "Item ${item.id} not found in dump items, ignoring")
            return
        }

        startCountDown(item, category = Category.Normal, handleCountdownFinished = {
            _dumpItems.removeAll { it.id == item.id }
            _items.add(item)

            sortAllList()
        })
    }

    fun onClickedCancelItem(item: Item) {
        countDownJobs.remove(item.id)?.cancel()
        updateItem(item.id, isActive = false, category = item.category) { it.copy(countdownSeconds = null) }
    }

    private fun startCountDown(item: Item, category: Category, handleCountdownFinished: () -> Unit) {
        val job = viewModelScope.launch {
            for(i in TIME_OUT downTo 0) {
                updateItem(item.id, isActive = true, category = category) { it.copy(countdownSeconds = i) }

                if(i == 0) {
                    handleCountdownFinished()
                }
                delay(1.seconds)
            }
        }

        countDownJobs[item.id] = job
    }

    private fun updateItem(itemId: Int, isActive: Boolean, category: Category, transform: (Item) -> Item) {
        val list = when(category.getCompareType()) {
            Type.NORMAL -> _items
            Type.DUMP -> _dumpItems
        }

        val idx = list.indexOfFirst { it.id == itemId }
        if (idx >= 0) {
            list[idx] = transform(list[idx]).copy(isActive = isActive, category = category)
        }
    }
    fun sortAllList() {
        _items.sortBy { it.id }
        _dumpItems.sortBy { it.id }
    }
}