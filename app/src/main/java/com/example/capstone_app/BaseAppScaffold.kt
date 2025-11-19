package com.example.capstone_app
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.annotation.NavigationRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.capstone_app.ui.theme.Capstone_AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAppScaffold(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
){
    Scaffold(
        topBar ={
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon ?: {},
                actions = actions ?: {},
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = content
    )
}


@Preview(showBackground = true, name = "1 기본 TopAppBar 화면")
@Composable
fun BaseAppScaffoldPreviuw(){
    Capstone_AppTheme {
        BaseAppScaffold(title= "기본 TopAppBar") {innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center

            ){
                Text("TopAppBar Content")
            }
        }
    }
}
@Preview(showBackground = true, name = "2 Dropdown 메뉴")
@Composable
fun DropdownMenuTopAppBar(){
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false)}

    BaseAppScaffold(
        title = "Dropdown 메뉴",
        actions = {
            Box{
                IconButton(onClick = { menuExpanded = true }){
                    Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = {menuExpanded = false}) {
                    DropdownMenuItem(text = {Text("설정")}, onClick = {
                        Toast.makeText(context, "설정 선택", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                    })
                    DropdownMenuItem(text = {Text("도움말")}, onClick = {
                        Toast.makeText(context, "도움말 선택", Toast.LENGTH_SHORT).show()
                        menuExpanded = false
                    })
                }
            }
        }
    ) {padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center){
            Text("DropdownMenu 예제")
        }
    }
}

@Preview(showBackground = true, name = "3 네비게이션 및 검색")
@Composable
fun NavigationAndActions(){
    val context  = LocalContext.current
    BaseAppScaffold(
        title = "네이게이션 & 검색",
        navigationIcon = {
            IconButton(onClick = {
                Toast.makeText(context, "메뉴 클릭", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Search, contentDescription = "검색")
            }
        }
    ){padding->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center){
            Text("내비게이션 + 검색")
        }
    }
}
@Preview(showBackground = true, name = "4. 네비 메뉴 + 검색 + 드롭다운 메뉴")
@Composable
fun NavigationSearchDropdown(){
    val context = LocalContext.current
}














