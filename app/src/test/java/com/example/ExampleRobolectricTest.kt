package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.math.AlgebraFormulaProvider
import com.example.math.BengaliNumberFormatter
import com.example.math.MultiplicationProvider
import com.example.math.PrimeNumberProvider
import com.example.math.QuizGenerator
import com.example.math.QuizTopic
import com.example.math.SquareCubeProvider
import com.example.math.TableRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Math Master", appName)
    }

    @Test
    fun `verify multiplication tables 1 to 40 in 4 ranges`() {
        assertEquals(4, TableRange.values().size)

        val range1 = MultiplicationProvider.getTablesForRange(TableRange.RANGE_1_10)
        val range2 = MultiplicationProvider.getTablesForRange(TableRange.RANGE_11_20)
        val range3 = MultiplicationProvider.getTablesForRange(TableRange.RANGE_21_30)
        val range4 = MultiplicationProvider.getTablesForRange(TableRange.RANGE_31_40)

        assertEquals(10, range1.size)
        assertEquals(10, range2.size)
        assertEquals(10, range3.size)
        assertEquals(10, range4.size)

        // Verify table 7
        val table7 = MultiplicationProvider.getTable(7)
        assertEquals(10, table7.rows.size)
        assertEquals(7, table7.rows[0].product) // 7 * 1
        assertEquals(70, table7.rows[9].product) // 7 * 10
    }

    @Test
    fun `verify prime numbers count in ranges`() {
        val primes1To100 = PrimeNumberProvider.primes1To100
        val primes101To200 = PrimeNumberProvider.primes101To200

        assertEquals(25, primes1To100.size)
        assertEquals(21, primes101To200.size)

        assertTrue(PrimeNumberProvider.isPrime(2))
        assertTrue(PrimeNumberProvider.isPrime(97))
        assertTrue(PrimeNumberProvider.isPrime(101))
        assertTrue(PrimeNumberProvider.isPrime(199))

        assertFalse(PrimeNumberProvider.isPrime(1))
        assertFalse(PrimeNumberProvider.isPrime(4))
        assertFalse(PrimeNumberProvider.isPrime(91)) // 7 * 13
        assertFalse(PrimeNumberProvider.isPrime(119)) // 7 * 17
    }

    @Test
    fun `verify squares and cubes from 1 to 20`() {
        val squareList = SquareCubeProvider.squareTableList
        val cubeList = SquareCubeProvider.cubeTableList

        assertEquals(20, squareList.size) // 1 to 20
        assertEquals(20, cubeList.size)   // 1 to 20

        val item1 = squareList.first { it.n == 1 }
        assertEquals("1² = 1", item1.squareText)
        assertEquals("√1 = 1", item1.squareRootText)

        val item12 = squareList.first { it.n == 12 }
        assertEquals("12² = 144", item12.squareText)
        assertEquals("√144 = 12", item12.squareRootText)

        val item20 = squareList.first { it.n == 20 }
        assertEquals("20² = 400", item20.squareText)
        assertEquals("√400 = 20", item20.squareRootText)

        val cube1 = cubeList.first { it.n == 1 }
        assertEquals("1³ = 1", cube1.cubeText)
        assertEquals("∛1 = 1", cube1.cubeRootText)

        val cube10 = cubeList.first { it.n == 10 }
        assertEquals("10³ = 1000", cube10.cubeText)
        assertEquals("∛1000 = 10", cube10.cubeRootText)

        val cube20 = cubeList.first { it.n == 20 }
        assertEquals("20³ = 8000", cube20.cubeText)
        assertEquals("∛8000 = 20", cube20.cubeRootText)
    }

    @Test
    fun `verify algebraic square and cube formulas`() {
        val squares = AlgebraFormulaProvider.squareFormulas
        val cubes = AlgebraFormulaProvider.cubeFormulas

        assertEquals(14, squares.size) // Exact 14 square formulas
        assertEquals(8, cubes.size)     // Exact 8 cube formulas

        assertTrue(squares.any { it.formulaLatex.contains("(a + b)² = a² + 2ab + b²") })
        assertTrue(squares.any { it.formulaLatex.contains("(a − b − c)²") })
        assertTrue(cubes.any { it.formulaLatex.contains("(a + b)³ = a³ + 3a²b + 3ab² + b³") })
        assertTrue(cubes.any { it.formulaLatex.contains("a³ − b³ = (a − b)(a² + ab + b²)") })
    }

    @Test
    fun `verify quiz question generator`() {
        val questions = QuizGenerator.generateQuiz(QuizTopic.MULTIPLICATION, 5)
        assertEquals(5, questions.size)
        questions.forEach { q ->
            assertEquals(4, q.optionsBn.size)
            assertTrue(q.correctIndex in 0..3)
        }
    }

    @Test
    fun `verify bengali numeral converter`() {
        assertEquals("০", BengaliNumberFormatter.toBn(0))
        assertEquals("১২৩৪৫", BengaliNumberFormatter.toBn(12345))
        assertEquals("১০০", BengaliNumberFormatter.toBn(100))
    }

    @Test
    fun `verify math viewmodel creation and streak initialization`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.MathViewModel(app)
        assertTrue(viewModel.streakDays.value >= 1)
        assertTrue(viewModel.longestStreak.value >= 1)
        assertEquals(7, viewModel.weeklyActivities.value.size)
        assertEquals(com.example.ui.Screen.Home, viewModel.currentScreen)
    }

    @Test
    fun `verify prime numbers magic code for 1 to 100 and 101 to 200`() {
        val magic100 = PrimeNumberProvider.magicCode1To100
        assertEquals("4422322321", magic100.codeString)
        assertEquals(25, magic100.totalPrimes)
        assertEquals(10, magic100.decadeGroups.size)
        assertEquals(25, magic100.decadeGroups.sumOf { it.magicDigit })
        assertEquals(listOf(4, 4, 2, 2, 3, 2, 2, 3, 2, 1), magic100.decadeGroups.map { it.magicDigit })

        val magic200 = PrimeNumberProvider.magicCode101To200
        assertEquals("4113122214", magic200.codeString)
        assertEquals(21, magic200.totalPrimes)
        assertEquals(10, magic200.decadeGroups.size)
        assertEquals(21, magic200.decadeGroups.sumOf { it.magicDigit })
        assertEquals(listOf(4, 1, 1, 3, 1, 2, 2, 2, 1, 4), magic200.decadeGroups.map { it.magicDigit })

        // Verify first decade of 1-100 has 4 primes: 2, 3, 5, 7
        assertEquals(listOf(2, 3, 5, 7), magic100.decadeGroups[0].primes)
        // Verify last decade of 1-100 has 1 prime: 97
        assertEquals(listOf(97), magic100.decadeGroups[9].primes)

        // Verify 101-110 has 4 primes: 101, 103, 107, 109
        assertEquals(listOf(101, 103, 107, 109), magic200.decadeGroups[0].primes)
        // Verify 191-200 has 4 primes: 191, 193, 197, 199
        assertEquals(listOf(191, 193, 197, 199), magic200.decadeGroups[9].primes)
    }

    @Test
    fun `verify customization state and persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.MathViewModel(app)

        // Initial default state
        assertEquals(com.example.ui.customization.CustomLayoutType.COMFORTABLE_LIST, viewModel.customizationState.value.global.layoutType)
        assertTrue(viewModel.customizationState.value.sections.isEmpty())

        // Update preview layout to GRID and configure section
        viewModel.updatePreviewGlobal { it.copy(layoutType = com.example.ui.customization.CustomLayoutType.GRID, gridColumns = 3) }
        viewModel.updatePreviewSection(com.example.ui.customization.SectionKey.TABLES) {
            it.copy(iconId = com.example.ui.customization.AvailableIcon.CALCULATE.id, iconSizeDp = 32, cornerRadiusDp = 20)
        }

        assertEquals(com.example.ui.customization.CustomLayoutType.GRID, viewModel.previewCustomizationState.value.global.layoutType)
        assertEquals(3, viewModel.previewCustomizationState.value.global.gridColumns)
        val secCfg = viewModel.previewCustomizationState.value.getSectionConfig(com.example.ui.customization.SectionKey.TABLES)
        assertEquals("calculate", secCfg.iconId)
        assertEquals(32, secCfg.iconSizeDp)
        assertEquals(20, secCfg.cornerRadiusDp)

        // Save customization
        viewModel.saveCustomization()
        assertEquals(com.example.ui.customization.CustomLayoutType.GRID, viewModel.customizationState.value.global.layoutType)

        // Discard preview returns to saved
        viewModel.updatePreviewGlobal { it.copy(layoutType = com.example.ui.customization.CustomLayoutType.COMPACT_LIST) }
        viewModel.discardCustomizationPreview()
        assertEquals(com.example.ui.customization.CustomLayoutType.GRID, viewModel.previewCustomizationState.value.global.layoutType)

        // Verify independent section reset
        viewModel.resetPreviewSection(com.example.ui.customization.SectionKey.TABLES)
        assertNull(viewModel.previewCustomizationState.value.sections[com.example.ui.customization.SectionKey.TABLES.id])

        // Verify reset all customizations restores defaults
        viewModel.resetAllCustomizations()
        assertEquals(com.example.ui.customization.CustomLayoutType.COMFORTABLE_LIST, viewModel.customizationState.value.global.layoutType)
        assertTrue(viewModel.customizationState.value.sections.isEmpty())
    }

    @Test
    fun `verify color contrast and hex validation`() {
        assertTrue(com.example.ui.customization.ColorUtils.isValidHex("#1E3A8A"))
        assertTrue(com.example.ui.customization.ColorUtils.isValidHex("1E3A8A"))
        assertFalse(com.example.ui.customization.ColorUtils.isValidHex("invalid_hex"))

        val darkBlue = com.example.ui.customization.ColorUtils.parseHexColor("#1E3A8A")
        assertNotNull(darkBlue)
        assertTrue(com.example.ui.customization.ColorUtils.hasAdequateContrast(androidx.compose.ui.graphics.Color.White, darkBlue!!))
    }
}
