package com.kotlinkhaos.classes.errors

import kotlinx.serialization.Serializable


@Serializable
data class KotlinKhaosApiError(val status: Int, val error: String)
