package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.BengaliNumberFormatter
import com.example.math.QuizTopic
import com.example.ui.MathViewModel
import com.example.ui.components.MathSectionHeader
import com.example.ui.components.MathTopAppBar

@Composable
fun QuizzesScreen(
    viewModel: MathViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val quizScores by viewModel.quizScores.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        MathTopAppBar(
            title = "ইন্টারেক্টিভ কুইজ",
            subtitle = "জ্ঞান যাচাই ও নৈর্ব্যক্তিক প্রস্তুতি",
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "কুইজ বিভাগ নির্বাচন করুন",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রতিটি কুইজে ১০টি করে প্রশ্ন থাকবে এবং তাত্ক্ষণিক সমাধান ও ফলাফল প্রদর্শিত হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Quiz Topics
            items(QuizTopic.values()) { topic ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startQuiz(topic, 10) }
                        .testTag("quiz_topic_${topic.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.titleBn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "১০টি প্রশ্ন • বহুনির্বাচনী পরীক্ষা",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "শুরু করুন",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Past Quiz Records
            if (quizScores.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    MathSectionHeader(
                        title = "সংরক্ষিত কুইজ স্কোরবোর্ড",
                        subtitle = "অফলাইন ডাটাবেসে সংরক্ষিত আপনার পূর্ববর্তী পরীক্ষার ফলাফল"
                    )
                }

                items(quizScores) { score ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = score.topicTitleBn,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "স্কোর: ${BengaliNumberFormatter.toBn(score.score)} / ${BengaliNumberFormatter.toBn(score.totalQuestions)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (score.percentage >= 80) Color(0xFF10B981) else if (score.percentage >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "${BengaliNumberFormatter.toBn(score.percentage)}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizPlayScreen(
    viewModel: MathViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val session by viewModel.quizSession.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        MathTopAppBar(
            title = session.topic.titleBn,
            subtitle = if (!session.isFinished)
                "প্রশ্ন ${BengaliNumberFormatter.toBn(session.currentIndex + 1)} / ${BengaliNumberFormatter.toBn(session.questions.size)}"
            else "ফলাফল",
            onBackClick = { viewModel.navigateBack() }
        )

        if (session.isFinished) {
            // Quiz Completed Result Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val total = session.questions.size
                val correct = session.correctCount
                val percentage = if (total > 0) (correct * 100) / total else 0

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (percentage >= 80) Color(0xFF10B981) else if (percentage >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${BengaliNumberFormatter.toBn(percentage)}%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (percentage >= 80) "অসাধারণ পারফরম্যান্স!" else if (percentage >= 50) "ভালো হয়েছে! আরও অনুশীলন করুন" else "আরও নিয়মিত পড়াশোনা প্রয়োজন",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "আপনি ${BengaliNumberFormatter.toBn(total)} টি প্রশ্নের মধ্যে ${BengaliNumberFormatter.toBn(correct)} টি সঠিক উত্তর দিয়েছেন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { viewModel.startQuiz(session.topic, session.questions.size) },
                    modifier = Modifier.fillMaxWidth().testTag("retake_quiz_btn")
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("আবার পরীক্ষা দিন")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("কুইজ তালিকায় ফিরে যান")
                }
            }
        } else {
            // Active Question
            val currentQ = session.questions.getOrNull(session.currentIndex)
            if (currentQ != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Progress Indicator
                    val progressRatio = (session.currentIndex + 1).toFloat() / session.questions.size.toFloat()
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = currentQ.questionBn,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options list
                    currentQ.optionsBn.forEachIndexed { optIndex, optionText ->
                        val isSelected = session.selectedOptionIndex == optIndex
                        val isCorrectOption = optIndex == currentQ.correctIndex

                        val containerColor = when {
                            session.isAnswerSubmitted && isCorrectOption -> Color(0xFF10B981).copy(alpha = 0.2f)
                            session.isAnswerSubmitted && isSelected && !isCorrectOption -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val outlineColor = when {
                            session.isAnswerSubmitted && isCorrectOption -> Color(0xFF10B981)
                            session.isAnswerSubmitted && isSelected && !isCorrectOption -> Color(0xFFEF4444)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable(enabled = !session.isAnswerSubmitted) {
                                    viewModel.selectQuizOption(optIndex)
                                }
                                .testTag("quiz_option_$optIndex"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, outlineColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = BengaliNumberFormatter.toBn(optIndex + 1),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                if (session.isAnswerSubmitted) {
                                    if (isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "সঠিক",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "ভুল",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Explanation Box after submit
                    AnimatedVisibility(visible = session.isAnswerSubmitted) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "ব্যাখ্যা:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentQ.explanationBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action buttons
                    if (!session.isAnswerSubmitted) {
                        Button(
                            onClick = { viewModel.submitQuizAnswer() },
                            enabled = session.selectedOptionIndex != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_answer_btn")
                        ) {
                            Text("উত্তর জমা দিন", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("next_question_btn")
                        ) {
                            Text(
                                text = if (session.currentIndex + 1 < session.questions.size) "পরবর্তী প্রশ্ন" else "ফলাফল দেখুন",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
