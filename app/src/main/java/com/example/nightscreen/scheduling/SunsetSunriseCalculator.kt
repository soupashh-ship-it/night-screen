package com.example.nightscreen.scheduling

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Pure Kotlin Astronomical calculation helper for local Sunset and Sunrise times.
 * Based on the Official US Naval Observatory algorithm.
 */
object SunsetSunriseCalculator {

    data class SolarTimes(
        val sunriseHour: Int,
        val sunriseMinute: Int,
        val sunsetHour: Int,
        val sunsetMinute: Int
    )

    /**
     * Calculates local sunrise and sunset times for a given day, latitude, and longitude.
     * Defaults to approx 18:30 sunset and 06:30 sunrise if coordinates are (0,0) or default.
     */
    fun calculateSolarTimes(
        cal: Calendar = Calendar.getInstance(),
        lat: Double = 0.0,
        lng: Double = 0.0
    ): SolarTimes {
        if (lat == 0.0 && lng == 0.0) {
            return SolarTimes(6, 30, 18, 30)
        }

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val tzOffsetHours = TimeZone.getDefault().getOffset(cal.timeInMillis) / 3600000.0

        val sunriseMinutes = calculateTime(dayOfYear, lat, lng, isSunrise = true)
        val sunsetMinutes = calculateTime(dayOfYear, lat, lng, isSunrise = false)

        val (srH, srM) = minutesToLocalTime(sunriseMinutes, tzOffsetHours)
        val (ssH, ssM) = minutesToLocalTime(sunsetMinutes, tzOffsetHours)

        return SolarTimes(srH, srM, ssH, ssM)
    }

    private fun calculateTime(dayOfYear: Int, lat: Double, lng: Double, isSunrise: Boolean): Double {
        val zenith = 90.833
        val lngHour = lng / 15.0
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - lngHour) / 24.0)
        } else {
            dayOfYear + ((18.0 - lngHour) / 24.0)
        }

        val M = (0.9856 * t) - 3.289

        var L = M + (1.916 * sin(Math.toRadians(M))) + (0.020 * sin(Math.toRadians(2 * M))) + 282.634
        L = normalizeAngle(L)

        var RA = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(L))))
        RA = normalizeAngle(RA)

        val lQuadrant = floor(L / 90.0) * 90.0
        val raQuadrant = floor(RA / 90.0) * 90.0
        RA += (lQuadrant - raQuadrant)
        RA /= 15.0

        val sinDec = 0.39782 * sin(Math.toRadians(L))
        val cosDec = cos(asin(sinDec))

        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))

        if (cosH > 1) return 360.0
        if (cosH < -1) return 1080.0

        val H = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }

        val HHours = H / 15.0
        val T = HHours + RA - (0.06571 * t) - 6.622
        val UT = T - lngHour
        return normalizeAngle(UT * 15.0) * 4.0
    }

    private fun normalizeAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun minutesToLocalTime(utcMinutes: Double, tzOffsetHours: Double): Pair<Int, Int> {
        var localMinutes = (utcMinutes + (tzOffsetHours * 60.0)).toInt() % 1440
        if (localMinutes < 0) localMinutes += 1440

        val hour = (localMinutes / 60) % 24
        val minute = localMinutes % 60
        return Pair(hour, minute)
    }
}
