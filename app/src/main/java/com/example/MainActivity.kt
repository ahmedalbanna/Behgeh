package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.BehgehAppContent
import com.example.ui.BehgehViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Instantiate our main ViewModel holding all persistence & AI logic
        val viewModel = ViewModelProvider(this)[BehgehViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                BehgehAppContent(viewModel = viewModel)
            }
        }
    }
}
