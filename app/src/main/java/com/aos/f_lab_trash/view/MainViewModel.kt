package com.aos.f_lab_trash.view

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.f_lab_trash.view.ui.theme.Category
import com.aos.f_lab_trash.view.ui.theme.Item
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class MainViewModel : ViewModel() {

    private var _items = mutableStateListOf<Item>()
    val items: List<Item> get() = _items
    private var _dumpItems = mutableStateListOf<Item>()
    val dumpItems: List<Item> get() = _dumpItems
    private val countDownJobs = ConcurrentHashMap<Int, Job>()
    private val itemLocks = ConcurrentHashMap<Int, Mutex>()

    companion object {
        const val TIME_OUT = 3
    }

    init {
        for (i in 1..50) {
            _items.add(
                Item(
                    id = i,
                    name = "item $i"
                )
            )
        }
    }

    fun onClickedDumpItem(item: Item) {
        viewModelScope.launch {
            val lock = itemLocks.computeIfAbsent(item.id) { Mutex() }
            if (lock.tryLock()) {
                try {
                    val done = CompletableDeferred<Unit>()

                    startCountDown(
                        item,
                        category = Category.DUMP,
                        handleCountdownFinished = {
                            // countdown 끝났을 때만 실행되는 후처리
                            _items.removeAll { it.id == item.id }
                            _dumpItems.add(item)
                            sortAllList()

                            done.complete(Unit)
                        }
                    )

                    done.await()
                } finally {
                    lock.unlock()
                }
            }
        }
    }

    fun onClickedRecoveryItem(item: Item) {
        viewModelScope.launch {
            val lock = itemLocks.computeIfAbsent(item.id) { Mutex() }
            if (lock.tryLock()) {
                try {
                    val done = CompletableDeferred<Unit>()

                    startCountDown(item, category = Category.NORMAL, handleCountdownFinished = {
                        _dumpItems.removeAll { it.id == item.id }
                        _items.add(item)

                        sortAllList()

                        done.complete(Unit)
                    })

                    done.await()
                } finally {
                    lock.unlock()
                }
            }
        }
    }

    fun onClickedCancelItem(item: Item) {
        countDownJobs.remove(item.id)?.cancel()
        itemLocks.remove(item.id)
        updateItem(item.id, category = item.category) { it.copy(countdownSeconds = null) }
    }

    private fun startCountDown(
        item: Item,
        category: Category,
        handleCountdownFinished: () -> Unit
    ) {
        val job = viewModelScope.launch {
            for (i in TIME_OUT downTo 0) {
                updateItem(item.id, category = category) { it.copy(countdownSeconds = i) }

                if (i == 0) {
                    handleCountdownFinished()
                }
                delay(1.seconds)
            }
        }

        countDownJobs[item.id] = job
    }

    private fun updateItem(itemId: Int, category: Category, transform: (Item) -> Item) {
        val list = when (category.opposite()) {
            Category.NORMAL -> _items
            Category.DUMP -> _dumpItems
        }

        val idx = list.indexOfFirst { it.id == itemId }
        if (idx >= 0) {
            list[idx] = transform(list[idx]).copy(category = category)
        }
    }

    fun sortAllList() {
        _items.sortBy { it.id }
        _dumpItems.sortBy { it.id }
    }
}