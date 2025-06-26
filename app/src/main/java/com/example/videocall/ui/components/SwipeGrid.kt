package com.example.videocall.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> SwipeGrid(
    items: List<T>,
    itemContent: @Composable (T, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerPage: Int = 4,
    itemSpacing: Dp = 8.dp,
) {
    val pages = (items.size + itemsPerPage - 1) / itemsPerPage
    val pagerState = rememberPagerState(pageCount = { pages })
    val orientation = LocalConfiguration.current.orientation

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        val start = page * itemsPerPage
        val end = minOf(start + itemsPerPage, items.size)
        val pageItems = items.subList(start, end)
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            SwipeGridContentPortrait(pageItems, itemContent, itemSpacing)
        } else {
            SwipeGridContentLandscape(pageItems, itemContent, itemSpacing)
        }
    }
}

@Composable
private fun <T> SwipeGridContentPortrait(
    pageItems: List<T>,
    itemContent: @Composable (T, Modifier) -> Unit,
    itemSpacing: Dp,
) {
    if (pageItems.size <= 3) {
        Column(
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            if (pageItems.size == 1)
                Spacer(Modifier.weight(1f / 3))
            pageItems.forEach {
                itemContent(
                    it, Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            if (pageItems.size == 1)
                Spacer(Modifier.weight(1f / 3))
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        for (i in 0..((pageItems.size - 1) / 2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemContent(
                    pageItems[i * 2], Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                if (i * 2 + 1 < pageItems.size)
                    itemContent(
                        pageItems[i * 2 + 1], Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
            }
        }
    }
}

@Composable
private fun <T> SwipeGridContentLandscape(
    pageItems: List<T>,
    itemContent: @Composable (T, Modifier) -> Unit,
    itemSpacing: Dp,
) {
    if (pageItems.size <= 3) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            if (pageItems.size == 1)
                Spacer(Modifier.weight(0.05f))
            pageItems.forEach {
                itemContent(
                    it, Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
            if (pageItems.size == 1)
                Spacer(Modifier.weight(0.05f))
        }
        return
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        for (i in 0..((pageItems.size - 1) / 2)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                itemContent(
                    pageItems[i * 2], Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                if (i * 2 + 1 < pageItems.size)
                    itemContent(
                        pageItems[i * 2 + 1], Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
            }
        }
    }
}