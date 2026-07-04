package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.MintCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyManagerTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(

        title = {
            Text(
                text = title,
                color = DeepSlate
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepSlate
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MintCream,
            titleContentColor = DeepSlate,
            navigationIconContentColor = DeepSlate,
            actionIconContentColor = DeepSlate
        )
    )
}
