package com.example.mattatoyomng.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class CoroutineScopes {
    companion object {
        val MAIN = CoroutineScope(SupervisorJob() + Dispatchers.Main);
        val IO = CoroutineScope(SupervisorJob() + Dispatchers.IO);
    }
}