package tech.miroslav.caloriecounter.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class AppConstantsTests {

    @Test
    fun `shared auth user id is fixed`() {
        val expected = UUID.fromString("7f761e74-2297-4b79-92f2-55307de133a4")
        assertThat(AppConstants.SHARED_AUTH_USER_ID).isEqualTo(expected)
    }

    @Test
    fun `activity multipliers are correct`() {
        assertThat(ActivityLevel.SEDENTARY.multiplier).isEqualTo(BigDecimal("1.2"))
        assertThat(ActivityLevel.LIGHTLY_ACTIVE.multiplier).isEqualTo(BigDecimal("1.375"))
        assertThat(ActivityLevel.MODERATELY_ACTIVE.multiplier).isEqualTo(BigDecimal("1.55"))
        assertThat(ActivityLevel.ACTIVE.multiplier).isEqualTo(BigDecimal("1.725"))
        assertThat(ActivityLevel.VERY_ACTIVE.multiplier).isEqualTo(BigDecimal("1.9"))
    }

    @Test
    fun `activity parsing supports enum name and display`() {
        assertThat(ActivityLevel.fromString("SEDENTARY")).isEqualTo(ActivityLevel.SEDENTARY)
        assertThat(ActivityLevel.fromString("sedentary")).isEqualTo(ActivityLevel.SEDENTARY)
        assertThat(ActivityLevel.fromString("Sedentary")).isEqualTo(ActivityLevel.SEDENTARY)
        assertThat(ActivityLevel.fromString("Lightly active")).isEqualTo(ActivityLevel.LIGHTLY_ACTIVE)
        assertThat(ActivityLevel.fromString("Moderately active")).isEqualTo(ActivityLevel.MODERATELY_ACTIVE)
        assertThat(ActivityLevel.fromString("Active")).isEqualTo(ActivityLevel.ACTIVE)
        assertThat(ActivityLevel.fromString("Very active")).isEqualTo(ActivityLevel.VERY_ACTIVE)
        assertThat(ActivityLevel.fromString("unknown")).isNull()
        assertThat(ActivityLevel.fromString(null)).isNull()
    }

    @Test
    fun `supported FTS configs include english and russian`() {
        assertThat(AppConstants.SUPPORTED_FTS_CONFIGS)
            .contains("english", "russian")
    }
}
