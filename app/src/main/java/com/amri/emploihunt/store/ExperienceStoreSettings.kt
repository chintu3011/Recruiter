package com.amri.emploihunt.store

import androidx.datastore.core.Serializer
import com.amri.emploihunt.proto.Experiences
import java.io.InputStream
import java.io.OutputStream

object ExperienceStoreSettings : Serializer<Experiences.ExperienceList> {
    override val defaultValue: Experiences.ExperienceList
        get() = Experiences.ExperienceList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Experiences.ExperienceList {
        return Experiences.ExperienceList.parseFrom(input)
    }

    override suspend fun writeTo(t: Experiences.ExperienceList, output: OutputStream) {
        return t.writeTo(output)
    }

}