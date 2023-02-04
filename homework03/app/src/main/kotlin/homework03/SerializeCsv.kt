package homework03

import com.soywiz.korio.async.use
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


fun <T: Any> csvSerialize(data: Iterable<T>, klass: KClass<T>) = buildString { serializeObject(data, klass) }

private fun <T: Any> StringBuilder.serializeObject(data: Iterable<T>, klass: KClass<T>) {
    serializeHeader(klass)

    if (data.any {
            it.javaClass.kotlin != klass
        }) throw IllegalArgumentException("not all types match")

    data.forEach {
        serializeObject(it)
    }
}

private fun StringBuilder.serializeNumber(value: Number) = apply {
    append(value)
}

private fun StringBuilder.serializeValue(value: Any?) = apply {
    if (value == null) {
        serializeString("null")
        return@apply
    }
    when (value.javaClass.kotlin) {
        String::class -> {
            serializeString(value as String)
        }
        Integer::class, Short::class, Long::class, Byte::class, Float::class, Double::class -> {
            serializeNumber(value as Number)
        }
    }
}

private fun StringBuilder.serializeString(value: String) = apply {
    append('"')
    append(value.replace("\"", "\"\""))
    append('"')
}

private fun <T: Any> StringBuilder.serializeHeader(klass: KClass<T>) = apply {
    append("")
    val properties = klass.memberProperties

    when (klass) {
        String::class -> {
            serializeString("value")
        }
        else -> {
            properties.joinTo(this, ",") { p ->
                serializeString(p.name)
                ""
            }
        }
    }
    append("\n")
}

private fun StringBuilder.serializeObject(value: Any) {
    when (value.javaClass.kotlin) {
        String::class -> {
            serializeString(value as String)
        }
        else -> {
            value.javaClass.kotlin.memberProperties.joinTo(this, ",") { p ->
                serializeValue(p.get(value))
                ""
            }
        }
    }
    append("\n")
}

suspend fun <T : Any> writeToCsv(data: Iterable<T>, klass: KClass<T>, file: String, path: String = localCurrentDirVfs.path)
    = localVfs(path)[file].open(VfsOpenMode.CREATE).use {
        writeString(csvSerialize(data, klass))
    }

