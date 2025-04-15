package au.edu.utas.jc101.aflstatsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    private lateinit var ui : ActivityMainBinding

    override fun onCreate(SavedInstanceState: Bundle?)
    {
        super.onCreate(SavedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.lblMainTitle.text = "Welcome to the AFL Stats App"
    }
}

