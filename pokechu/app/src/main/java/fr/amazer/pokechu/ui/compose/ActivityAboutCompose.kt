package fr.amazer.pokechu.ui.compose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import fr.amazer.pokechu.BuildConfig
import fr.amazer.pokechu.R
import fr.amazer.pokechu.ui.compose.ui.theme.Pokechu_appTheme

class ActivityAboutCompose : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pokechu_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController: NavHostController = rememberNavController()
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = stringResource(R.string.title_activity_about))
                                },
                                navigationIcon = {
                                    IconButton(onClick = {navController.popBackStack()}) {
                                        Icon(Icons.Filled.ArrowBack, "backIcon")
                                    }
                                },
                            )
                        }, content = {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Content()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Content() {
//    val (ref_1) = createRefs()

    Column(verticalArrangement = Arrangement.Center) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(R.drawable.ic_pokeball), contentDescription = "", modifier = Modifier.width(23.dp).height(23.dp))
            Text(text = BuildConfig.APPLICATION_ID)
            Row {
                Column {
                    Text(text = stringResource(R.string.about_version_name))
                    Text(text = stringResource(R.string.about_version_code))
                    Text(text = stringResource(R.string.about_build_date))
                    Text(text = stringResource(R.string.about_build_time))
                }
                Column {
                    Text(text = BuildConfig.VERSION_NAME)
                    Text(text = BuildConfig.VERSION_CODE.toString())
                    Text(text = BuildConfig.BUILD_DATE)
                    Text(text = BuildConfig.BUILD_TIME)
                }
            }
            Image(painter = painterResource(R.drawable.ic_pokeball), contentDescription = "", modifier = Modifier.width(23.dp).height(23.dp))
            Text(text = stringResource(R.string.about_created_by))
            Text(text = "@")
            Image(painter = painterResource(R.drawable.ic_logo_amazer), contentDescription = "", modifier = Modifier.width(100.dp).height(50.dp))
            Image(painter = painterResource(R.drawable.ic_pokeball), contentDescription = "", modifier = Modifier.width(23.dp).height(23.dp))
            Text(text = stringResource(R.string.about_thanks), textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Pokechu_appTheme {
        Content()
    }
}