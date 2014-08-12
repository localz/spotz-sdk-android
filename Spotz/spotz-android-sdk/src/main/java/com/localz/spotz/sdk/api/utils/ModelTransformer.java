package com.localz.spotz.sdk.api.utils;

import android.content.Context;

public interface ModelTransformer<T, V> {
    V transform(Context context, T model);
}
