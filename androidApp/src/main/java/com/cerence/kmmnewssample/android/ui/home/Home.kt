package com.cerence.kmmnewssample.android.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.cerence.kmmnewssample.android.R
import com.cerence.kmmnewssample.android.ui.components.HomeAppBar
import com.cerence.kmmnewssample.android.ui.components.Loader
import com.cerence.kmmnewssample.domain.domain_model.NewsDataModel
import com.cerence.kmmnewssample.presentation.NewsEvent
import com.cerence.kmmnewssample.presentation.NewsState
import com.cerence.kmmnewssample.presentation.NewsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun Home(
    destinationsNavigator: DestinationsNavigator,
    viewModel: NewsViewModel = getViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val controller = rememberNavController()
    val openDrawer = {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Drawer(onDestinationClicked = { route ->
                    scope.launch { drawerState.close() }
                    controller.navigate(route) {
                        launchSingleTop = true
                        popUpTo(controller.graph.startDestDisplayName!!)
                    }
                })
            }
        },
    ) {
        NavHost(
            navController = controller,
            startDestination = DrawerScreens.Home.route
        ) {
            composable(DrawerScreens.Home.route) {
                //screen content
                LaunchedEffect(key1 = Unit, block = {
                    viewModel.newsIntent(NewsEvent.GetHeadlines)
                })

                val state by viewModel.state.collectAsState()
                News(
                    state = state,
                    destinationsNavigator = destinationsNavigator,
                    drawerState = drawerState,
                    scope = scope,
                    openDrawer = {
                        openDrawer()
                    }
                )
            }

            composable(DrawerScreens.Movie.route) {
                Movies(
                    destinationsNavigator = destinationsNavigator,
                    drawerState = drawerState,
                    scope = scope,
                    openDrawer = {
                        openDrawer()
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawer(
    destinationsNavigator: DestinationsNavigator,
    viewModel: NewsViewModel = getViewModel(),
    startDestination: String = "News"
) {

}

sealed class DrawerScreens(val title: String,val route: String) {
    object Home : DrawerScreens("News","News")
    object Movie : DrawerScreens("Movies", "Movies")
    object Help : DrawerScreens( "Help", "Help")
}
private val screens = listOf(
    DrawerScreens.Home,
    DrawerScreens.Movie,
    DrawerScreens.Help
)

@Composable
fun Drawer(
    modifier: Modifier = Modifier,
    onDestinationClicked: (route: String) -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 48.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_home_24),
            contentDescription = "App icon"
        )
        screens.forEach { screen ->
            Spacer(Modifier.height(24.dp))
            Text(
                text = screen.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.clickable {
                    onDestinationClicked(screen.route)
                }
            )
        }
    }
}

@Composable
fun DrawerBody(navController: DestinationsNavigator?, closeNavDrawer: () -> Unit) {
    Column {
        DrawerMenuItem(
            iconDrawableId = R.drawable.baseline_home_24,
            text = "News",
            onItemClick = {
                navController?.navigate("News")
                closeNavDrawer()
            }
        )
        DrawerMenuItem(
            iconDrawableId = R.drawable.baseline_movie_24,
            text = "Movies",
            onItemClick = {
                navController?.navigate("Movies")
                closeNavDrawer()
            }
        )
    }
}


@Composable
private fun DrawerMenuItem(
    iconDrawableId: Int,
    text: String,
    onItemClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ){
        Icon(
            painter = painterResource(iconDrawableId),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text )
    }
}


@Composable
fun DrawerHeader(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp)
        ,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = "News", fontSize = 60.sp)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun HomePage(
    state: NewsState,
    destinationsNavigator: DestinationsNavigator,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    // to keep track if the animation is playing
    // and play pause accordingly
    var isPlaying by remember {
        mutableStateOf(true)
    }

    // for speed
    var speed by remember {
        mutableStateOf(1f)
    }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        HomeAppBar(
            titleRes = R.string.app_heading,
            actionIcons = { },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                }) {
                    Icon(imageVector = Icons.Default.Menu,contentDescription = null)
                }
            }
        )
    }) { innerPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumedWindowInsets(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
            }

            when (state) {
                is NewsState.Error -> {
                    isPlaying = !isPlaying
                    item {
                        Text(text = state.errorMsg, style = MaterialTheme.typography.headlineMedium)
                    }
                }

                is NewsState.Idle -> {
                    isPlaying = !isPlaying
                }
                is NewsState.Loading -> {
                    placeHolder(isPlaying, speed)
                }

                is NewsState.Success -> {
                    isPlaying = !isPlaying
//                    headlines(state.list, destinationsNavigator)
                }
            }
        }
    }
}

private fun LazyListScope.placeHolder(isPlaying: Boolean, speed: Float) {
    item {
        Column(modifier = Modifier.size(100.dp,100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
            Loader(isPlaying,speed)
        }
    }
}

//private fun LazyListScope.headlines(
//    list: List<NewsDataModel>,
//    destinationsNavigator: DestinationsNavigator
//) {
//    items(list) { item ->
//        HeadlinesCard(item, destinationsNavigator)
//    }
//}
//
//@Composable
//fun HeadlinesCard(item: NewsDataModel, destinationsNavigator: DestinationsNavigator) {
//    Card(
//        modifier = Modifier
//            .height(280.dp)
//            .fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(4.dp),
//        shape = MaterialTheme.shapes.medium,
//        border = BorderStroke(0.2.dp, MaterialTheme.colorScheme.surface)
//    ) {
//
//        Column {
//            AsyncImage(
//                modifier = Modifier
//                    .height(150.dp)
//                    .fillMaxWidth(),
//                model = ImageRequest.Builder(LocalContext.current)
//                    .scale(Scale.FILL).data(item.urlToImage).crossfade(true).build(),
//                contentDescription = item.title,
//                contentScale = ContentScale.Crop
//            )
//
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = item.title,
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = item.author ?: "",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                Text(
//                    text = item.source.name.ifEmpty { "" },
//                    style = MaterialTheme.typography.bodySmall.copy(
//                        color = MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.Bold
//                    )
//                )
//            }
//        }
//    }
//}
