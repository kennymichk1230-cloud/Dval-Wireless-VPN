package com.example

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.VpnAppScreen
import com.example.ui.VpnViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: VpnViewModel by viewModels()

    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.connect()
        } else {
            Toast.makeText(this, "VPN permission denied. Cannot establish tunnel.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VpnAppScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onRequestVpnPermission = {
                        prepareVpn()
                    },
                    onToggleConnection = {
                        handleToggleConnection()
                    }
                )
            }
        }
    }

    private fun handleToggleConnection() {
        if (viewModel.connectionState.value == com.wireguard.android.backend.Tunnel.State.UP || viewModel.isConnecting.value) {
            viewModel.disconnect()
        } else {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                vpnPrepareLauncher.launch(intent)
            } else {
                viewModel.connect()
            }
        }
    }

    private fun prepareVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPrepareLauncher.launch(intent)
        }
    }
}
