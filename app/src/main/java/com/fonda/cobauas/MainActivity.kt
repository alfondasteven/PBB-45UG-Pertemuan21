package com.fonda.cobauas

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.RoundedCorner
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.fonda.cobauas.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val REQUEST_CODE = 72
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var title: String
    private lateinit var actionBar: androidx.appcompat.app.ActionBar
    private lateinit var firebaseAuth: FirebaseAuth

    private val storageReference  = FirebaseStorage.getInstance().getReference("uploads")

    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //conf action bar
        actionBar = supportActionBar!!
        actionBar.title = "Home"

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle logout
        binding.buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        setImageViewHome()
        initAction()
    }

    //fungsi delete
    private fun deleteImage(title: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            storageReference.child(title).delete().await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"Delete Success",Toast.LENGTH_SHORT).show()
                resetLayout()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                binding.inputTextTitle.error = e.message
                binding.progressBarLoadingIndicator.visibility = View.GONE
            }
        }
    }

    //function donwload image dari firebase
    private fun donwloadImage(title: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDonwloadSize = 5L * 1024 * 1024
            val bytes = storageReference.child(title).getBytes(maxDonwloadSize).await()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.size)

            withContext(Dispatchers.Main){
                binding.imageViewHome.load(bitmap){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(15f))
                }
                binding.progressBarLoadingIndicator.visibility = View.GONE
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                binding.inputTextTitle.error = e.message
                binding.progressBarLoadingIndicator.visibility = View.GONE
                setImageViewHome()
            }
        }
    }

    //fungsi untuk reset tampilan lasyout
    private fun resetLayout(){
        setImageViewHome()
        imageUri = null
        binding.inputTextTitle.error = null
        binding.editTextTitle.text?.clear()
        binding.progressBarLoadingIndicator.visibility = View.GONE
        binding.textViewIndicatorLoading.visibility = View.GONE
    }

    //fungsi upload
    private fun uploadImage(title : String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageUri?.let { uri ->
                storageReference.child(title).putFile(uri)
                    .addOnProgressListener {
                        val progress: Int = ((100 * it.bytesTransferred) / it.totalByteCount).toInt()
                        binding.progressBarLoadingIndicator.progress = progress
                        val indicatorText = "Loading.. $progress%"
                        binding.textViewIndicatorLoading.text = indicatorText
                    }.await()

                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,"Success Uploaded...",Toast.LENGTH_SHORT).show()
                    delay(3000L)
                    resetLayout()
                }
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
                resetLayout()
            }
        }
    }

    //fungciton untuk mengambil gambar yang telah di pilih
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            data?.data?.let {
                imageUri = it
                binding.imageViewHome.load(imageUri){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(15f))
                }
            }
        }
    }

    //function mendeklarasikan aksi pada aplikasi
    private fun initAction(){
        binding.buttonSelectImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE)
            }
        }
        binding.buttonUploadImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            if (imageUri != null){
                if (title.isBlank()|| title.isEmpty()){
                    binding.inputTextTitle.error = "Required*"
                    Toast.makeText(this,"Name isRequired",Toast.LENGTH_SHORT).show()
                } else{
                    binding.progressBarLoadingIndicator.isIndeterminate = false
                    binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                    binding.textViewIndicatorLoading.visibility = View.VISIBLE
                    binding.inputTextTitle.error = null
                    uploadImage(title)
                }
            }else {
                Toast.makeText(this,"Pilih Album Lagu",Toast.LENGTH_LONG).show()
            }


        }
        binding.buttonDownloadImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            if(title.isEmpty() || title.isEmpty()){
                binding.inputTextTitle.error = "*Required"
            }else{
                binding.progressBarLoadingIndicator.isIndeterminate = true
                binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                binding.inputTextTitle.error = null
                donwloadImage(title)
            }
        }

        binding.buttonDeleteImage.setOnClickListener {
            title = binding.editTextTitle.text.toString().trim()
            if(title.isEmpty() || title.isEmpty()){
                binding.inputTextTitle.error = "*Required"
            }else{
                binding.progressBarLoadingIndicator.isIndeterminate = true
                binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                binding.inputTextTitle.error = null
                deleteImage(title)
            }
        }
        binding.buttonShowAllImage.setOnClickListener {
            startActivity(Intent(this,ShowListActivity::class.java))
        }

    }

    //function untuk mengatur imageview
    private fun setImageViewHome(){
        binding.imageViewHome.load(ContextCompat.getDrawable(this,R.drawable.shape)){
            crossfade(true)
            crossfade(500)
            transformations(RoundedCornersTransformation(15f))
        }
    }
    private fun checkUser(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            val email = firebaseUser.email
        }
        else{
            startActivity(Intent(this,Login::class.java))
        }
    }

}