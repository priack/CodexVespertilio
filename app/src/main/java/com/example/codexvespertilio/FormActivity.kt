package com.example.codexvespertilio

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var dateField: EditText
    private lateinit var locationField: EditText
    private lateinit var sexField: Spinner
    private lateinit var tagField: EditText
    private lateinit var extraField: EditText
    private lateinit var addButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        dateField = findViewById(R.id.dateField)
        locationField = findViewById(R.id.locationField)
        sexField = findViewById(R.id.sexField)
        tagField = findViewById(R.id.tagField)
        extraField = findViewById(R.id.extraField)
        addButton = findViewById(R.id.addButton)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Populate date field
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        dateField.setText(currentDate)

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLocation()
        }

        // Request write permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
        }

        addButton.setOnClickListener {
            addSighting()
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        locationField.setText("${it.latitude}, ${it.longitude}")
                    }
                }
        }
    }

    private fun addSighting() {
        val name = intent.getStringExtra("name")
        val date = dateField.text.toString()
        val location = locationField.text.toString()
        val sex = sexField.selectedItem.toString()
        val tag = tagField.text.toString()
        val extra = extraField.text.toString()

        val filePath = File(filesDir, "sights.csv")
        try {
            val writer = CSVWriter(FileWriter(filePath, true))
            writer.writeNext(arrayOf(name, date, location, sex, tag, extra))
            writer.close()
            println("Data successfully written to $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error writing to file: ${e.message}")
        }

        finish()
    }
}