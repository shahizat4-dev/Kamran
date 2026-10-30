package com.example

import com.example.model.UnitConversions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFeetAndInchesToDecimalFeet() {
        val result = UnitConversions.feetAndInchesToDecimalFeet(16.0, 6.0)
        assertEquals(16.5, result, 0.001)

        val result2 = UnitConversions.feetAndInchesToDecimalFeet(7.0, 3.0)
        assertEquals(7.25, result2, 0.001)
    }

    @Test
    fun testRectangularCftCalculation() {
        // 16.5 ft (L) * 7.5 ft (W) * 4.0 ft (H) = 495.0 CFT
        val cft = UnitConversions.calculateRectangularCft(16.5, 7.5, 4.0)
        assertEquals(495.0, cft, 0.001)
    }

    @Test
    fun testCftToBrassConversion() {
        // 1 Brass = 100 CFT
        val brass = UnitConversions.cftToBrass(495.0)
        assertEquals(4.95, brass, 0.001)

        val cftBack = UnitConversions.brassToCft(4.95)
        assertEquals(495.0, cftBack, 0.001)
    }

    @Test
    fun testCubicMetersAndYardsConversion() {
        val cft = 353.146667
        val cuM = UnitConversions.cftToCubicMeters(cft)
        assertEquals(10.0, cuM, 0.01)

        val cftForYards = 270.0
        val yards = UnitConversions.cftToCubicYards(cftForYards)
        assertEquals(10.0, yards, 0.01)
    }

    @Test
    fun testWeightToCftConversion() {
        // Sand density: 1600 kg/m3. 16 metric tons = 10 m3 = ~353.15 CFT
        val cft = UnitConversions.weightTonsToCft(16.0, 1600.0)
        assertEquals(353.15, cft, 0.1)

        val tonsBack = UnitConversions.cftToWeightMetricTons(cft, 1600.0)
        assertEquals(16.0, tonsBack, 0.01)
    }

    @Test
    fun testFormatCurrency() {
        val formatted = UnitConversions.formatCurrency(26010.50)
        assertTrue(formatted.contains("26,010.50"))
    }
}
