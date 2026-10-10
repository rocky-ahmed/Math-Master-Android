package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookmarkEntity
import com.example.math.BengaliNumberFormatter
import com.example.math.PrimeNumberInfo
import com.example.math.PrimeNumberProvider
import com.example.math.PrimeRange
import com.example.ui.MathViewModel
import com.example.ui.components.BookmarkIconButton
import com.example.ui.components.MathTopAppBar

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrimesScreen(
    viewModel: MathViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPrimeInfo by remember { mutableStateOf<PrimeNumberInfo?>(null) }
    var searchInput by remember { mutableStateOf("") }
    var searchedNumberInfo by remember { mutableStateOf<PrimeNumberInfo?>(null) }

    val ranges = listOf(PrimeRange.RANGE_1_100, PrimeRange.RANGE_101_200)
    val activeRange = ranges[selectedTabIndex]

    val primes = if (activeRange == PrimeRange.RANGE_1_100) {
        PrimeNumberProvider.primes1To100
    } else {
        PrimeNumberProvider.primes101To200
    }

    Column(modifier = modifier.fillMaxSize()) {
        MathTopAppBar(
            title = "মৌলিক সংখ্যা (১ - ২০০)",
            subtitle = "১-১০০ তে ২৫টি এবং ১০১-২০০ তে ২১টি",
            onBackClick = { viewModel.navigateBack() }
        )

        // Range Tab Selector
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth().testTag("primes_tab_row")
        ) {
            ranges.forEachIndexed { index, range ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = if (index == 0) "১ থেকে ১০০ (২৫টি)" else "১০১ থেকে ২০০ (২১টি)",
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Interactive Number Checker Box (Allows testing ANY number 1 to 200 or beyond)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "মৌলিক সংখ্যা যাচাইকারী (Checker)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "যেকোনো সংখ্যা লিখুন এবং দেখুন সেটি মৌলিক নাকি যৌগিক:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchInput,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(4)
                                    searchInput = digits
                                    val num = digits.toIntOrNull()
                                    searchedNumberInfo = if (num != null && num > 0) {
                                        PrimeNumberProvider.getPrimeInfo(num)
                                    } else null
                                },
                                label = { Text("সংখ্যা লিখুন (যেমন: ৭৩)") },
                                modifier = Modifier.weight(1f).testTag("prime_search_input"),
                                singleLine = true
                            )
                        }

                        if (searchedNumberInfo != null) {
                            val info = searchedNumberInfo!!
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (info.isPrime) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (info.isPrime) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (info.isPrime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = info.explanationBn,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (info.isPrime) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overview explanation header card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTabIndex == 0)
                                "১ থেকে ১০০ এর মধ্যে মোট ২৫টি মৌলিক সংখ্যা রয়েছে। ক্ষুদ্রতম ও একমাত্র জোড় মৌলিক সংখ্যা হলো ২।"
                            else
                                "১০১ থেকে ২০০ এর মধ্যে মোট ২১টি মৌলিক সংখ্যা রয়েছে। এদের প্রতিটি বিজোড় সংখ্যা।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Grid of Prime Numbers
            item {
                Text(
                    text = "${activeRange.titleBn}:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Render numbers as neat selectable cards
            val chunkedPrimes = primes.chunked(3)
            items(chunkedPrimes.size) { chunkIdx ->
                val chunk = chunkedPrimes[chunkIdx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chunk.forEach { prime ->
                        val primeId = "prime_$prime"
                        val isBookmarked = bookmarks.any { it.id == primeId }

                        ElevatedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPrimeInfo = PrimeNumberProvider.getPrimeInfo(prime) }
                                .testTag("prime_card_$prime"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = BengaliNumberFormatter.toBn(prime),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "মৌলিক",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Fill row space if less than 3
                    repeat(3 - chunk.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Detail Dialog on tapping any Prime
    if (selectedPrimeInfo != null) {
        val info = selectedPrimeInfo!!
        val itemId = "prime_${info.number}"
        val isBookmarked = bookmarks.any { it.id == itemId }

        AlertDialog(
            onDismissRequest = { selectedPrimeInfo = null },
            title = {
                Text(
                    text = "${info.bnNumber} - সংখ্যা বিশ্লেষণ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = info.explanationBn,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "গুণনীয়কসমূহ: ${info.factors.joinToString(", ") { BengaliNumberFormatter.toBn(it) }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPrimeInfo = null }) {
                    Text("ঠিক আছে")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleBookmark(
                            BookmarkEntity(
                                id = itemId,
                                category = "PRIME",
                                titleBn = "${info.bnNumber} (মৌলিক সংখ্যা)",
                                subtitleBn = "মৌলিক সংখ্যা (১-২০০)",
                                detailBn = info.explanationBn
                            )
                        )
                    }
                ) {
                    Text(if (isBookmarked) "বুকমার্ক মুছুন" else "বুকমার্ক করুন")
                }
            }
        )
    }
}
