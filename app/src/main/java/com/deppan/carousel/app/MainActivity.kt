package com.deppan.carousel.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val adapter by lazy {
        ItemAdapter { position: Int, _: Item ->
            Toast.makeText(this@MainActivity, "Pos $position", Toast.LENGTH_SHORT).show()
            recyclerView.smoothScrollToPosition(position)
        }
    }

    private val possibleItems = listOf(
        Item("Airplanes", R.drawable.ic_airplane_24dp),
        Item("Cars", R.drawable.ic_car_24dp),
        Item("Food", R.drawable.ic_fastfood_24dp),
        Item("Gas", R.drawable.ic_local_gas_station_24dp),
        Item("Home", R.drawable.ic_home_24dp)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.adapter = adapter
        adapter.setItems(possibleItems)
    }
}

data class Item(val title: String, @DrawableRes val icon: Int)