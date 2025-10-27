package tech.miroslav.caloriecounter.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class HealthCalculatorTests {

    @Test
    fun `bmi returns expected value`() {
        val bmi = HealthCalculator.bmi(BigDecimal("70"), BigDecimal("175"))
        // 70 / (1.75^2) = 22.857... => 22.86
        assertThat(bmi).isEqualTo(BigDecimal("22.86"))
    }

    @Test
    fun `bmr mifflin st jeor male`() {
        val dob = LocalDate.now().minusYears(30)
        val bmr = HealthCalculator.bmrMifflinStJeor("male", dob, BigDecimal("70"), BigDecimal("175"), LocalDate.now())
        // 10*70 + 6.25*175 - 5*30 + 5 = 700 + 1093.75 - 150 + 5 = 1648.75
        assertThat(bmr).isEqualTo(BigDecimal("1648.75"))
    }

    @Test
    fun `bmr mifflin st jeor female`() {
        val dob = LocalDate.now().minusYears(25)
        val bmr = HealthCalculator.bmrMifflinStJeor("female", dob, BigDecimal("60"), BigDecimal("165"), LocalDate.now())
        // 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 -161 = 1345.25
        assertThat(bmr).isEqualTo(BigDecimal("1345.25"))
    }

    @Test
    fun `estimated daily intake uses activity multiplier`() {
        val tdee = HealthCalculator.estimatedDailyIntake(BigDecimal("1600"), "Moderately active")
        assertThat(tdee).isEqualTo(BigDecimal("2480.00"))
    }
}
