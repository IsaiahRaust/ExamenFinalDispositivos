package edu.universidad.rotacionganado

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.universidad.rotacionganado.ui.ListaPotrerosFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListaPotrerosFragment())
                .commit()
        }
    }
}