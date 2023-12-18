package io.wollinger.animals.utils

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import org.w3c.dom.url.URLSearchParams
import org.w3c.xhr.XMLHttpRequest

inline fun <reified T> id(id: String): T {
    val element = document.getElementById(id)
    if(element is T) return element
    throw Exception("\"$id\" is not of type ${T::class.simpleName}!")
}

fun getParams() = URLSearchParams(window.location.search)

fun Window.setSearchParam(key: String, value: String) {
    debug("setSearchParam(key=$key, value=$value)")
    val params = getParams()
    params.set(key, value)
    window.history.pushState("", "", "?$params")
}

fun Window.removeSearchParam(key: String) {
    debug("removeSearchParam(key=$key)")
    val params = getParams()
    params.delete(key)
    window.history.pushState("", "", "?$params")
}

inline fun <reified T> dl(url: String, crossinline onSuccess: (T) -> Unit) {
    debug("dl(url=$url, onSuccess=...)")
    download(url) {
        onSuccess.invoke(Json.decodeFromString<T>(it))
    }
}

fun download(url: String, onSuccess: (String) -> Unit) {
    debug("download(url=$url, onSuccess=...)")
    XMLHttpRequest().apply {
        open("GET", url)
        send()
        onreadystatechange = {
            if(readyState == XMLHttpRequest.DONE && status == 200.toShort())
                onSuccess(responseText)
        }
    }

}

fun Double.toFixed(digits: Int) = asDynamic().toFixed(2)