package com.isodev.tinderui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.isodev.tinderui.ui.theme.TinderUiTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TinderUiTheme {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(
                            containerColor = Color.White,
                            contentColor = Color.White,
                        ) {
                            BottomBar()
                        }
                    },
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = topAppBarColors(
                                containerColor = Color.White,
                                titleContentColor = Color.White,
                            ),
                            title = {
                                Image(
                                    painter = painterResource(R.drawable.tinder_logo),
                                    contentDescription = ""
                                )
                            }
                        )
                    }) { innerPadding ->
                    SwipeCardExample(
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeCardExample(modifier: Modifier) {
    val images = remember {
        mutableStateListOf(
            CardData(0, R.drawable.card1, name = "Paulo", age = 25, city = "London", distance = 10),
            CardData(
                1,
                R.drawable.card2,
                name = "Jessica",
                age = 23,
                city = "New York",
                distance = 5
            ),
            CardData(2, R.drawable.card3, name = "Bethy", age = 24, city = "Paris", distance = 15),
            CardData(3, R.drawable.card4, name = "John", age = 26, city = "Tokyo", distance = 20),
            CardData(4, R.drawable.card5, name = "Diego", age = 22, city = "Sydney", distance = 8),
        )
    }
    var currentImageIndex by remember { mutableStateOf(0) }
    val iconStates = remember { mutableStateMapOf<Int, Pair<Boolean, Boolean>>() }
    val cardModifier = Modifier.size(392.dp, 618.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        for (i in 0 until images.size) {
            val imageIndex = (currentImageIndex - i + images.size) % images.size
            SwipeCard(
                modifier = cardModifier
                    .zIndex((images.size + i).toFloat()),
                cardData = images[imageIndex],
                onSwipeLeft = { cardData ->
                    iconStates[cardData.id] = true to false
                },
                onSwipeRight = { cardData ->
                    iconStates[cardData.id] = false to true
                },
                onDragEnd = { cardData ->
                    iconStates[cardData.id] = false to false
                    currentImageIndex = (currentImageIndex + 1) % images.size
                },
                sensitivityFactor = 3f
            ) { cardData ->
                Card(
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Image(
                            painter = painterResource(images[imageIndex].image),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                        )
                        NameAgeCity(modifier = modifier.offset(y = 20.dp), cardData = cardData)
                        val (favIcon, closeIcon) = iconStates[cardData.id] ?: (false to false)
                        Icons(
                            modifier = modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 85.dp),
                            favIcon = favIcon,
                            closeIcon = closeIcon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeCard(
    modifier: Modifier,
    cardData: CardData,
    onSwipeLeft: (CardData) -> Unit = {},
    onSwipeRight: (CardData) -> Unit = {},
    onDragEnd: (CardData) -> Unit = {},
    sensitivityFactor: Float = 3f,
    content: @Composable (CardData) -> Unit,
) {
    var offset by remember { mutableStateOf(0f) }
    var dismissRight by remember { mutableStateOf(false) }
    var dismissLeft by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    LaunchedEffect(dismissRight) {
        if (dismissRight) {
            onSwipeRight.invoke(cardData)
            dismissRight = false
        }
    }

    LaunchedEffect(dismissLeft) {
        if (dismissLeft) {
            onSwipeLeft.invoke(cardData)
            dismissLeft = false
        }
    }
    var dragStarted by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .offset { IntOffset(offset.roundToInt(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { dragStarted = true },
                onDragEnd = {
                    dragStarted = false
                    offset = 0f
                    onDragEnd(cardData)
                    dismissLeft = false
                    dismissRight = false
                }
            ) { change, dragAmount ->
                if (dragStarted) {
                    offset += (dragAmount / density) * sensitivityFactor
                }

                if (offset > 0) {
                    dismissRight = true
                } else if (offset < 0) {
                    dismissLeft = true
                }

                if (change.positionChange() != Offset.Zero) change.consume()
            }
        }
        .graphicsLayer(
            alpha = 10f - animateFloatAsState(if (dismissRight) 1f else 0f).value,
            rotationZ = animateFloatAsState(offset / 50).value
        )) {
        content(cardData)
    }
}

@Composable
fun BottomBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.tinder_icon),
                contentDescription = ""
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.star),
                contentDescription = ""
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.search),
                contentDescription = ""
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.message_icon),
                contentDescription = ""
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun Icons(modifier: Modifier, favIcon: Boolean, closeIcon: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(painter = painterResource(R.drawable.back), contentDescription = "")
        Image(
            painter = if (closeIcon) {
                painterResource(R.drawable.x__1_)
            } else {
                painterResource(R.drawable.x)
            }, contentDescription = ""
        )
        Image(painter = painterResource(R.drawable.star__1_), contentDescription = "")
        Image(
            painter = if (favIcon) {
                painterResource(R.drawable.like__1_)
            } else {
                painterResource(R.drawable.like)
            }, contentDescription = ""
        )
        Image(painter = painterResource(R.drawable.boost), contentDescription = "")
    }
}

@Composable
fun NameAgeCity(modifier: Modifier = Modifier, cardData: CardData) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = cardData.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                fontSize = 38.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = cardData.age.toString(),
                color = Color.White,
                fontSize = 25.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.residence),
                contentDescription = "City",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Lives in ${cardData.city}",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.location),
                contentDescription = "City",
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${cardData.distance} kilometers away",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

data class CardData(
    val id: Int,
    val image: Int,
    var favIcon: Boolean = false,
    var closeIcon: Boolean = false,
    var name: String,
    var age: Int,
    var city: String,
    var distance: Int
)