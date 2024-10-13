package com.example.codexvespertilio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opencsv.CSVReader
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var loadButton: Button
    private lateinit var namesRecyclerView: RecyclerView
    private lateinit var namesAdapter: NamesAdapter
    private lateinit var filterSpinner: Spinner
    private var allNames = mutableListOf<String>()
    private var seenNames = mutableListOf<String>()
    private var notSeenNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton = findViewById(R.id.loadButton)
        namesRecyclerView = findViewById(R.id.namesRecyclerView)
        filterSpinner = findViewById(R.id.filterSpinner)

        namesRecyclerView.layoutManager = LinearLayoutManager(this)
        namesAdapter = NamesAdapter(emptyList()) { name ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }
        namesRecyclerView.adapter = namesAdapter

        loadButton.setOnClickListener {
            loadNames()
            loadButton.visibility = View.GONE
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterNames(parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadNames() {
        val reader = CSVReader(InputStreamReader(assets.open("batlist.csv")))
        reader.readNext() // Skip header
        var nextLine: Array<String>?
        while (reader.readNext().also { nextLine = it } != null) {
            nextLine?.let {
                allNames.add(it[0])
            }
        }
        reader.close()

        val filePath = File(filesDir, "sights.csv")
        if (filePath.exists()) {
            val sightReader = CSVReader(InputStreamReader(filePath.inputStream()))
            sightReader.readNext() // Skip header
            while (sightReader.readNext().also { nextLine = it } != null) {
                nextLine?.let {
                    seenNames.add(it[0])
                }
            }
            sightReader.close()
        }

        notSeenNames = allNames.filter { it !in seenNames }.toMutableList()
        namesAdapter.updateNames(allNames)
    }

    private fun filterNames(filter: String) {
        when (filter) {
            "All" -> namesAdapter.updateNames(allNames)
            "Seen" -> namesAdapter.updateNames(seenNames)
            "Not seen" -> namesAdapter.updateNames(notSeenNames)
        }
    }
}