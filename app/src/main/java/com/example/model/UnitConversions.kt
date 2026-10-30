package com.example.model

import java.util.Locale

object UnitConversions {

    /**
     * Converts feet and inches to total decimal feet.
     * e.g., 14 ft 6 in = 14.5 ft
     */
    fun feetAndInchesToDecimalFeet(feet: Double, inches: Double): Double {
        return feet + (inches / 12.0)
    }

    /**
     * Converts decimal feet back to feet and rounded inches.
     */
    fun decimalFeetToFeetAndInches(decimalFeet: Double): Pair<Int, Double> {
        val ft = decimalFeet.toInt()
        val inches = (decimalFeet - ft) * 12.0
        return Pair(ft, inches)
    }

    /**
     * Calculates volume of standard rectangular truck body in Cubic Feet (CFT).
     * Dimensions passed in decimal feet.
     */
    fun calculateRectangularCft(lengthFt: Double, widthFt: Double, heightFt: Double): Double {
        return lengthFt * widthFt * heightFt
    }

    /**
     * Calculates volume of trapezoidal hopper truck body (slanted dump sides).
     * ((Top Width + Bottom Width) / 2) * Length * Height
     */
    fun calculateTrapezoidCft(
        lengthFt: Double,
        topWidthFt: Double,
        bottomWidthFt: Double,
        heightFt: Double
    ): Double {
        val avgWidth = (topWidthFt + bottomWidthFt) / 2.0
        return lengthFt * avgWidth * heightFt
    }

    /**
     * Calculates volume of cylindrical pile or column in CFT.
     * Pi * r^2 * h
     */
    fun calculateCylinderCft(radiusFt: Double, heightFt: Double): Double {
        return Math.PI * radiusFt * radiusFt * heightFt
    }

    /**
     * 1 Brass = 100 Cubic Feet (CFT).
     * Common official construction unit for sand, gravel, and stone in civil engineering.
     */
    fun cftToBrass(cft: Double): Double {
        return cft / 100.0
    }

    fun brassToCft(brass: Double): Double {
        return brass * 100.0
    }

    /**
     * 1 Cubic Meter = 35.3147 CFT
     */
    fun cftToCubicMeters(cft: Double): Double {
        return cft / 35.3146667
    }

    fun cubicMetersToCft(cuM: Double): Double {
        return cuM * 35.3146667
    }

    /**
     * 1 Cubic Yard = 27 CFT
     */
    fun cftToCubicYards(cft: Double): Double {
        return cft / 27.0
    }

    fun cubicYardsToCft(cuYd: Double): Double {
        return cuYd * 27.0
    }

    /**
     * Calculates estimated metric tons from CFT based on material density (kg/m3).
     */
    fun cftToWeightMetricTons(cft: Double, densityKgPerM3: Double): Double {
        val volumeM3 = cftToCubicMeters(cft)
        val weightKg = volumeM3 * densityKgPerM3
        return weightKg / 1000.0
    }

    /**
     * Calculates CFT from weight in metric tons and density.
     */
    fun weightTonsToCft(tons: Double, densityKgPerM3: Double): Double {
        if (densityKgPerM3 <= 0.0) return 0.0
        val weightKg = tons * 1000.0
        val volumeM3 = weightKg / densityKgPerM3
        return cubicMetersToCft(volumeM3)
    }

    fun formatNumber(value: Double, decimals: Int = 2): String {
        return String.format(Locale.US, "%.${decimals}f", value)
    }

    fun formatCurrency(amount: Double): String {
        return String.format(Locale.US, "$%,.2f", amount)
    }
}
