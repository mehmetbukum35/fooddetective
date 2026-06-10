package com.mehmetbukum.fooddetective.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "additives",
    indices = [Index(value = ["code"], unique = true)]
)
data class Additive(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("code")
    val code: String, // NOT NULL

    @SerializedName("name_tr")
    val name_tr: String, // NOT NULL

    @SerializedName("functional_class")
    val functional_class: String?,

    @SerializedName("halal_status")
    val halal_status: String?,

    @SerializedName("health_status")
    val health_status: String?,

    @SerializedName("risk_level")
    val risk_level: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("warning")
    val warning: String?,

    @SerializedName("name_en")
    val name_en: String?,

    @SerializedName("functional_class_en")
    val functional_class_en: String?,

    @SerializedName("health_status_en")
    val health_status_en: String?,

    @SerializedName("description_en")
    val description_en: String?,

    @SerializedName("warning_en")
    val warning_en: String?,

    @SerializedName("aliases_tr")
    val aliases_tr: String? = null,

    @SerializedName("aliases_en")
    val aliases_en: String? = null,

    @SerializedName("updated_at")
    val updated_at: String? = null
)
