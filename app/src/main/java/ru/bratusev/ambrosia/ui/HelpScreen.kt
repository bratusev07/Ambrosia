package ru.bratusev.ambrosia.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.bratusev.ambrosia.R

@Composable
fun HelpScreen() {
    val helpData: Map<Int, String> = mapOf(
        R.drawable.ambrose1 to "Куст амброзии до цветения",
        R.drawable.ambrose2 to "Куст амброзии во время цветения",
        R.drawable.ambrose3 to "Листья амброзии похожи на папоротник, верхние имеют компьвидную форму, " +
                "нижние — яйцевидную или ромбовидную.\n\n" +
                "Самые верхние листья обычно значительно меньше в размерах и менее разделены, чем нижние. " +
                "Также на листьях присутствуют волоски, особенно на нижней стороны листа.",
        R.drawable.ambrose4 to "Форма соцветий — колосья, состоящие из маленьких цветков, чаще всего" +
                " образуются на верхних стеблях, 3–4 см в длину, у некоторых видов могут доходить до" +
                "7–10см.\n\nЦветение происходит в основном летом и ранней осенью.",
        R.drawable.ambrose5 to "Стебель растения прямой, имеет красноватый или коричневато-зелёный цвет." +
                "\n\n На стебле амброзии всегда присутствуют щетинки."
    )

    val samePlants: Map<Int, String> = mapOf(
        R.drawable.ambrose6 to "1. Золотарник -  цветки трубчатые, лимонного цвета, у амброзии цвет соцветий зелёный.",
        R.drawable.ambrose7 to "2. Полынь обыкновенная - отличие от амброзии в том что на стебле нет волосков, другая форма цветка и более бледный цвет."
    )

    LazyColumn(Modifier.background(Color.White)) {
        item {
            Text(
                "Как выглядит амброзия?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight(700)
                )
            )
        }

        helpData.forEach {
            item {
                AmbroseItem(data = it.key to it.value)
            }
        }

        item {
            Text(
                "На какие расстения похожа амброзия:",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight(700)
                )
            )
        }

        samePlants.forEach {
            item {
                AmbroseItem(data = it.key to it.value)
            }
        }
    }
}

@Composable
fun AmbroseItem(modifier: Modifier = Modifier, data: Pair<Int, String>) {
    Column(modifier = modifier.fillMaxWidth()) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 40.dp)
                .fillMaxWidth(),
            painter = painterResource(data.first),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Text(
            data.second,
            textAlign = if (data.second.split("\n").size == 1) TextAlign.Center else TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp, top = 10.dp, bottom = 20.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight(400)
            ),
        )
    }
}

@Preview
@Composable
fun HelpScreenPreview(modifier: Modifier = Modifier) {
    HelpScreen()
}