package com.gtkim.mobile_access_control.feature.history.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gtkim.mobile_access_control.feature.common.ui.theme.AppTheme
import java.time.ZoneId
import com.gtkim.mobile_access_control.feature.common.ui.component.AppLoadingIndicator
import com.gtkim.mobile_access_control.feature.common.ui.dialog.AppDialog
import com.gtkim.mobile_access_control.feature.history.ui.component.AccessLogCard
import com.gtkim.mobile_access_control.feature.history.ui.component.HistoryFilterBar
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = hiltViewModel()
    val state by viewModel.collectAsState()

    HistoryScaffold(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScaffold(
    state: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    /**
     * 마지막에서 3번째 아이템이 노출되면 다음 페이지 요청. derivedStateOf 로 visibleItemsInfo 변화에서
     * 임계 통과 순간만 true 가 되도록 묶어 LaunchedEffect 재실행을 최소화한다.
     */
    val shouldLoadMore by remember(state.endReached, state.loadingPage) {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 3 && !state.endReached && !state.loadingPage
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onIntent(HistoryIntent.LoadNext)
    }

    // 첫 진입 / 필터 변경으로 리스트가 비어 있는 동안의 로딩은 화면 정중앙에 통일된
    // AppLoadingIndicator 로 표시 (Stats 화면과 동일). PullToRefreshBox 의 자체 인디케이터
    // (상단 작은 원형) 는 데이터가 이미 있는 상태에서 사용자가 명시적으로 당겼을 때만 노출된다.
    val showFullscreenLoading = state.items.isEmpty() && (state.isRefreshing || state.loadingPage)

    if (showFullscreenLoading) {
        AppLoadingIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            HistoryFilterBar(
                filter = state.filter,
                zoneId = state.zoneId,
                onFilterChange = { onIntent(HistoryIntent.FilterChanged(it)) },
            )
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(HistoryIntent.Refresh) },
                state = pullState,
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.items, key = { it.id }) { log ->
                        AccessLogCard(log = log, zoneId = state.zoneId)
                    }
                    if (state.loadingPage && !state.isRefreshing) {
                        item {
                            AppLoadingIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    state.dialog?.let { dialog ->
        AppDialog(
            dialog = dialog,
            onDismiss = { onIntent(HistoryIntent.DialogDismissed) },
            onConfirm = { onIntent(HistoryIntent.DialogConfirmed) },
        )
    }
}

@Preview
@Composable
private fun HistoryScaffoldPreview() {
    AppTheme {
        HistoryScaffold(
            state = HistoryUiState(zoneId = ZoneId.of("Asia/Tokyo"), isRefreshing = false),
            onIntent = {},
        )
    }
}
