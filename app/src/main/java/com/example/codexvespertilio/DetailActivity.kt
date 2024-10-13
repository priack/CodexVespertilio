package com.example.codexvespertilio

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.opencsv.CSVReader
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.Html
import java.io.*

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var changeImageButton: Button
    private val PICK_IMAGE_REQUEST = 1
    private var imageName: String? = null
    private lateinit var detailsTextView: TextView
    private lateinit var sightsTable: TableLayout
    private lateinit var openFormButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imageView = findViewById(R.id.imageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        detailsTextView = findViewById(R.id.detailsTextView)
        sightsTable = findViewById(R.id.sightsTable)
        openFormButton = findViewById(R.id.openFormButton)

        val name = intent.getStringExtra("name")
        supportActionBar?.title = name
        loadDetails(name)
        loadSights(name)

        openFormButton.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }

        changeImageButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                saveImageToAssets(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImageToAssets(bitmap: Bitmap) {
        val assetManager = assets
        val imagePath = "images/Default/$imageName.jpg"
        val file = File(filesDir, imagePath)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadDetails(name: String?) {
        val reader = CSVReader(InputStreamReader(assets.open("batlist.csv")))
        reader.readNext() // Skip header
        var nextLine: Array<String>?
        while (reader.readNext().also { nextLine = it } != null) {
            nextLine?.let {
                if (it[0] == name) {
                    // Load image
                    val imagePath = "images/Default/" + it[0].lowercase() + ".jpg"
                    val assetManager = assets
                    try {
                        val inputStream = assetManager.open(imagePath)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageView.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    // Load details
                    val details = """
                    <b> English:</b> ${it[1]}<br>
                    <b>Spanish:</b> ${it[2]}<br>
                    <b> Weight:</b> ${it[3]}<br>
                    <b> Forearm:</b> ${it[4]}<br>
                    <b> Peak Frequency:</b> ${it[5]}<br>
                    <b> Frequency range:</b> ${it[6]}
                """.trimIndent()
                    detailsTextView.text = Html.fromHtml(details, Html.FROM_HTML_MODE_LEGACY)

                    // Load description
                    val description = "<b> Description:</b> ${it[7]}"
                    findViewById<TextView>(R.id.descriptionTextView).text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
                    return
                }
            }
        }
        reader.close()
    }

    private fun loadSights(name: String?) {
        val filePath = File(filesDir, "sights.csv")

        // Check if the file exists, if not create it and write the header line
        if (!filePath.exists()) {
            filePath.createNewFile()
            filePath.writeText("Bat,Date,Location,Sex,Tag,Extra\n")
        }

        val reader = CSVReader(InputStreamReader(filePath.inputStream()))
        reader.readNext() // Skip header

        // Create header row
        val headerRow = TableRow(this)
        val headers = arrayOf("Date", "Location", "Sex", "Tag")
        for (header in headers) {
            val headerCell = TextView(this)
            headerCell.text = header
            headerCell.setTypeface(null, android.graphics.Typeface.BOLD)
            headerCell.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            headerRow.addView(headerCell)
        }
        sightsTable.addView(headerRow)

        // Add data rows
        var nextLine: Array<String>?
        while (reader.readNext().also { nextLine = it } != null) {
            nextLine?.let {
                if (it[0] == name) {
                    val row = TableRow(this)

                    // Format Date
                    val date = it[1].split(" ")[0]

                    // Format Location
                    val location = it[2].split(",").map { coord ->
                        String.format("%.2f", coord.toDouble())
                    }.joinToString(", ")

                    // Format Sex
                    val sex = when (it[3].lowercase()) {
                        "male" -> "M"
                        "female" -> "F"
                        else -> it[3]
                    }

                    val formattedData = arrayOf(date, location, sex, it[4])
                    for (data in formattedData) {
                        val cell = TextView(this)
                        cell.text = data
                        cell.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        row.addView(cell)
                    }
                    sightsTable.addView(row)
                }
            }
        }
        reader.close()
    }
}