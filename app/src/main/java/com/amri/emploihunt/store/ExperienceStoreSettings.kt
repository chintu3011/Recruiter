package com.amri.emploihunt.store

import android.content.Context
import android.widget.Toast
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.amri.emploihunt.proto.Experiences
import java.io.InputStream
import java.io.OutputStream

object ExperienceStoreSettings : Serializer<Experiences.ExperienceList> {
    override val defaultValue: Experiences.ExperienceList
        get() = Experiences.ExperienceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Experiences.ExperienceList {
        try {
            return Experiences.ExperienceList.parseFrom(input)
        }
        catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    }

    override suspend fun writeTo(t: Experiences.ExperienceList, output: OutputStream) {
        return t.writeTo(output)
    }

}