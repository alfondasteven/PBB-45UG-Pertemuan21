package com.fonda.cobauas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.fonda.cobauas.databinding.ActivityShowListBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class ShowListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowListBinding

    private val storageReference = FirebaseStorage.getInstance().getReference("Uploads")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "All Lists"

        getAllImage()

    }
    //fungsi membaca db
    private fun getAllImage() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = storageReference.listAll().await()
            val imageUrls = mutableListOf<String>()
            for (image in images.items){
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main){
                val listAdapter = ListAdapter(imageUrls)
                if (listAdapter.itemCount == 0){
                    binding.textViewNoData.visibility = View.VISIBLE
                }
                binding.progressLoadList.visibility = View.GONE
                binding.recyclerViewImage.apply {
                    adapter = listAdapter
                    layoutManager = LinearLayoutManager(this@ShowListActivity)
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                binding.progressLoadList.visibility = View.GONE
                Toast.makeText(this@ShowListActivity, e.message , Toast.LENGTH_LONG).show()
            }
        }
    }
}