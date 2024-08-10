@file:OptIn(ExperimentalMaterial3Api::class)

package layout

import scene.SceneViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Preview
@Composable
fun Main() {
    BoxWithConstraints {
        if (maxWidth.value > 1000) {
            TwoColumnsLayout()
        } else {
            SingleColumnLayout()
        }
    }

}

@Composable
fun TwoColumnsLayout() {
    Row(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth(0.15f), contentAlignment = Alignment.Center) {
            ScenesLayout()
        }
        CurrentScene()
    }
}


@Composable
fun SingleColumnLayout(sceneViewModel: SceneViewModel = koinInject()) {
    val currentScene = sceneViewModel.scene.value
    if (currentScene == null) {
        ScenesLayout()
    } else {
        Column {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "#${currentScene::class.simpleName}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    sceneViewModel.scene.value = null
                                }
                            ) {
                                Text("back")
                            }
                        }
                    )
                },
                content = {
                    CurrentScene()
                }
            )
        }
    }
}


@Composable
fun ScenesLayout() {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WGPU4K showcase",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge
            )
        }

        ScenesBody()
    }
}

@Composable
fun ScenesBody(sceneViewModel: SceneViewModel = koinInject()) {
    val scenes = listOf(
        "Hello triangle",
        "Hello triangle",
        "Hello triangle",
        "Hello triangle",
        "Hello triangle",
        "Hello triangle",
        "Hello triangle",
        "Hello triangle"
    )
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxSize()
    ) {
        scenes.forEach { scene ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        sceneViewModel.color.value += 1
                        if (sceneViewModel.color.value > 4) {
                            sceneViewModel.color.value = 2
                        }
                    }
                ) {
                    Text(
                        text = scene,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
