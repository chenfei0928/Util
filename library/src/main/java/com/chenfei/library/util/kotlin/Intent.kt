package com.chenfei.library.util.kotlin

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable

fun Intent.putParcelableListExtra(name: String, value: List<Parcelable>?): Intent {
    return putParcelableArrayListExtra(name, value?.toArrayList())
}

fun Bundle.putParcelableList(name: String, value: List<Parcelable>?) {
    putParcelableArrayList(name, value?.toArrayList())
}
