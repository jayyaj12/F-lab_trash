package com.aos.f_lab_trash.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aos.f_lab_trash.R
import com.aos.f_lab_trash.view.ui.theme.Flab_trashTheme
import com.aos.f_lab_trash.view.ui.theme.Item
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Flab_trashTheme {
                val viewModel: MainViewModel by viewModels()
                Column {
                    Header()
                    HorizontalDivider(
                        Modifier, DividerDefaults.Thickness, DividerDefaults.color
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ListScreen(
                            Modifier.weight(1f),
                            items = viewModel.items,
                            onClickedDumpItem = { item ->
                                viewModel.onClickedDumpItem(item)
                            },
                            onCanceledDupItem = { item ->
                                viewModel.onClickedCancelItem(item)
                            })

                        XmlListScreen(
                            modifier = Modifier
                                .weight(1f)
                                .clipToBounds(),
                            items = viewModel.dumpItems.toList(),
                            onClickedRecoveryItem = { item ->
                                viewModel.onClickedRecoveryItem(item)
                            },
                            onCanceledRecoveryItem = { item ->
                                viewModel.onClickedCancelItem(item)
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "목록", modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
            Text(
                "휴지통", modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    items: List<Item>,
    onCanceledDupItem: (Item) -> Unit,
    onClickedDumpItem: (Item) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items, key = { it.id }) { item ->
            ListItem(item = item, onCanceledDupItem = { item ->
                onCanceledDupItem(item)
            }, onClickedDumpItem = { item ->
                onClickedDumpItem(item)
            })
            HorizontalDivider()
        }
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    item: Item,
    onCanceledDupItem: (Item) -> Unit,
    onClickedDumpItem: (Item) -> Unit
) {
    Column {
        Text(
            text = item.name, modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            val label = item.countdownSeconds?.let { "${it}초 후 삭제" } ?: "대기중"
            Text(
                text = label
            )
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = {
                onCanceledDupItem(item)
            }) {
                Text("취소")
            }
        }

        Icon(
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    onClickedDumpItem(item)
                },
            painter = painterResource(R.drawable.baseline_restore_from_trash_24),
            contentDescription = "dump Icon"
        )
    }
}

@Composable
fun XmlListScreen(
    modifier: Modifier = Modifier,
    items: List<Item>,
    onClickedRecoveryItem: (Item) -> Unit,
    onCanceledRecoveryItem: (Item) -> Unit
) {
    AndroidView(
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                // LazyColumn과 동일하게 패딩 제거
                setPadding(0, 0, 0, 0)
                adapter = ListAdapter(
                    onClickedRecoveryItem = onClickedRecoveryItem,
                    onCanceledRecoveryItem = onCanceledRecoveryItem
                )
                addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                )
            }
        }, update = { recyclerView ->
            (recyclerView.adapter as ListAdapter).submitList(items)
        }, modifier = modifier
    )
}