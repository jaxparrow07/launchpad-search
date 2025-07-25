package com.devrinth.launchpad.search.plugins

import android.content.Context
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.devrinth.launchpad.R
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import java.util.regex.Pattern

class UnitConversionPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var ID = "units"

    private fun convertLength(input: String): String {

        val unitsToMeters = mapOf(
            "mm" to 0.001, "millimeter" to 0.001, "millimeters" to 0.001,
            "cm" to 0.01, "centimeter" to 0.01, "centimeters" to 0.01,
            "m" to 1.0, "meter" to 1.0, "meters" to 1.0,
            "km" to 1000.0, "kilometer" to 1000.0, "kilometers" to 1000.0,
            "in" to 0.0254, "inch" to 0.0254, "inches" to 0.0254,
            "ft" to 0.3048, "foot" to 0.3048, "feet" to 0.3048,
            "yd" to 0.9144, "yard" to 0.9144, "yards" to 0.9144,
            "mi" to 1609.34, "mile" to 1609.34, "miles" to 1609.34
        )

        return convert(input, unitsToMeters)

    }

    private fun convertMass(input: String): String {
        val unitsToKilograms = mapOf(
            "mg" to 0.000001, "milligram" to 0.000001, "milligrams" to 0.000001,
            "g" to 0.001, "gram" to 0.001, "grams" to 0.001,
            "kg" to 1.0, "kilogram" to 1.0, "kilograms" to 1.0,
            "lb" to 0.453592, "pound" to 0.453592, "pounds" to 0.453592,
            "oz" to 0.0283495, "ounce" to 0.0283495, "ounces" to 0.0283495,
            "ton" to 1000.0, "tons" to 1000.0
        )

        return convert(input, unitsToKilograms)
    }

    private fun convertTemperature(input: String): String {
        val pattern = Pattern.compile("""(\d+(\.\d+)?)\s*([a-zA-Z]+)\s*(to|in)\s*([a-zA-Z]+)""")
        val matcher = pattern.matcher(input)

        if (!matcher.find()) {
            throw IllegalArgumentException("Invalid input format")
        }

        val value = matcher.group(1).toDouble()
        val fromUnit = matcher.group(3).lowercase()
        val toUnit = matcher.group(5).lowercase()

        val kelvinValue = when (fromUnit) {
            "c", "celsius" -> value + 273.15
            "f", "fahrenheit" -> (value + 459.67) * 5.0 / 9.0
            "k", "kelvin" -> value
            else -> throw IllegalArgumentException("Unknown unit")
        }

        val convertedValue = when (toUnit) {
            "c", "celsius" -> kelvinValue - 273.15
            "f", "fahrenheit" -> kelvinValue * 9.0 / 5.0 - 459.67
            "k", "kelvin" -> kelvinValue
            else -> throw IllegalArgumentException("Unknown unit")
        }

        return mContext.getString(R.string.plugin_unit_conversion_result).format(convertedValue, toUnit)
    }

    private fun convert(input: String, conversionMap: Map<String, Double>): String {
        val pattern = Pattern.compile("""(\d+(\.\d+)?)\s*([a-zA-Z]+)\s*(to|in)\s*([a-zA-Z]+)""")
        val matcher = pattern.matcher(input)

        if (!matcher.find()) {
            throw IllegalArgumentException("Invalid input format")
        }

        val value = matcher.group(1).toDouble()
        val fromUnit = matcher.group(3).lowercase()
        val toUnit = matcher.group(5).lowercase()

        if (!conversionMap.containsKey(fromUnit) || !conversionMap.containsKey(toUnit)) {
            throw IllegalArgumentException("Unknown unit")
        }

        val valueInSIUnit = value * conversionMap[fromUnit]!!
        val convertedValue = valueInSIUnit / conversionMap[toUnit]!!

        val formatterString = if (convertedValue != 0.toDouble()) {
            mContext.getString(R.string.plugin_unit_conversion_result_full) } else {
            mContext.getString(R.string.plugin_unit_conversion_result) }

        return formatterString.format(convertedValue, toUnit)
    }

    private fun returnUnits(input: String) : String {
        val pattern = Pattern.compile("""(\d+(\.\d+)?)\s*([a-zA-Z]+)\s*(to|in)\s*([a-zA-Z]+)""")
        val matcher = pattern.matcher(input)

        if (!matcher.find()) {
            throw IllegalArgumentException("Invalid input format")
        }

        return mContext.getString(R.string.plugin_unit_conversion_result_units).format(matcher.group(3), matcher.group(5))

    }

    override fun pluginProcess(query: String) {
        super.pluginProcess(query)
        try {
            val input = query.trim().lowercase()
            val result = when {
                input.contains(Regex("m|meter|inch|yard|mile|foot|feet|cm|mm|km|in")) ->
                    ResultAdapter(
                        convertLength(input),
                        returnUnits(input),
                        AppCompatResources.getDrawable(mContext, R.drawable.baseline_ruler_24),
                        null,
                        null

                    )

                input.contains(Regex("g|gram|kg|kilogram|lb|pound|oz|ounce|ton|mg")) ->
                    ResultAdapter(
                        convertMass(input),
                        returnUnits(input),
                        AppCompatResources.getDrawable(mContext, R.drawable.baseline_scale_24),
                        null,
                        null
                    )

                input.contains(Regex("c|celsius|f|fahrenheit|k|kelvin")) ->
                    ResultAdapter(
                        convertTemperature(input),
                        returnUnits(input),
                        AppCompatResources.getDrawable(mContext, R.drawable.baseline_thermostat_24),
                        null,
                        null
                    )

                else -> throw IllegalArgumentException("Unknown conversion type")
            }

            pluginResult(
                arrayListOf(result),
                query
            )

        } catch (e: Exception) {
        }
    }
}