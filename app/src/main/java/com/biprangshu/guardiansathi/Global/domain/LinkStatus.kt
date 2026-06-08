package com.biprangshu.guardiansathi.Global.core.domain

data class LinkStatus(
    val isLinked: Boolean,
    val linkedUid: String?,
    //introduced new list for list of elders
    val linkedElders: List<String> = emptyList()
)
