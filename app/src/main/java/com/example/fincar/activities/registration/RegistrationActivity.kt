package com.example.fincar.activities.registration

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.fincar.R
import com.example.fincar.app.Tools
import com.example.fincar.app.Tools.showToast
import com.example.fincar.bean.Account
import com.example.fincar.databinding.ActivityRegistrationBinding
import com.example.fincar.extensions.loadImage
import com.example.fincar.network.firebase.upload.UploadDataCallbacks
import com.example.fincar.network.firebase.upload.Uploader
import com.example.fincar.network.firebase_storage.FirebaseStorageHelper
import com.example.fincar.network.firebase_storage.UploadFileListener
import kotlinx.android.synthetic.main.activity_registration.*
import java.text.DateFormat
import java.util.*

const val EXTRA_USER = "extra-user"

class RegistrationActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var imageUri: Uri? = null
    private var imageUrl: String = ""

    private var currentAccount: Account? = null
    private lateinit var binding: ActivityRegistrationBinding

    private val uploader = Uploader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)

        getUserFromIntent(intent)

        setListeners()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Tools.PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            profileImageView.loadImage(imageUri)
        }

    }

    private fun setListeners() {

        binding.chooseImageLayout.setOnClickListener {
            Tools.openImageChooser(this)
        }

        binding.birthDateTextView.setOnClickListener {
            Tools.showDatePickerDialog(supportFragmentManager)
        }

        binding.saveButton.setOnClickListener {

            if (isEveryFieldValid()) {
                registerUser()
            }

        }
    }

    private fun isEveryFieldValid(): Boolean {
        return (Validator.isValidEmail(binding.emailInputLayout.editText?.text.toString(), binding)
                && Validator.isPhoneValid(
            binding.phoneInputLayout.editText?.text.toString(),
            binding
        )
                && Validator.isFirstNameValid(
            binding.firstNameInputLayout.editText?.text.toString(),
            binding
        )
                && Validator.isLastNameValid(
            binding.lastNameInputLayout.editText?.text.toString(),
            binding
        )
                && Validator.isBirthDateValid(binding.birthDateTextView.text.toString()))
    }

    private fun getUserFromIntent(intent: Intent) {
        if (intent.hasExtra(EXTRA_USER)) {
            currentAccount = intent.getParcelableExtra(EXTRA_USER) as Account
            binding.user = currentAccount
            profileImageView.loadImage(Uri.parse(currentAccount!!.imageUrl))
            binding.chooseImageLayout.isClickable = false
        }
    }

    private fun registerUser() {

        if (imageUri != null) {
            FirebaseStorageHelper.putFileToStorage(imageUri!!, object : UploadFileListener {
                override fun onSuccess(url: String) {
                    imageUrl = url
                    writeDataToRD()
                }

                override fun onError(message: String) {
                    showToast(this@RegistrationActivity, message)
                }

            })
        } else {
            imageUrl = DEFAULT_IMAGE_URL
            writeDataToRD()
        }

    }

    private fun writeDataToRD() {
        val user = userModelWithCurrentData()
        user.imageUrl = imageUrl
        uploader.upload(Account::class.java, user, uploadUserEventListener)
    }

    private fun userModelWithCurrentData() = Account(
        binding.emailInputLayout.editText?.text.toString(),
        binding.phoneInputLayout.editText?.text.toString(),
        binding.firstNameInputLayout.editText?.text.toString(),
        binding.lastNameInputLayout.editText?.text.toString(),
        binding.birthDateTextView.text.toString(),
        binding.locationInputLayout.editText?.text.toString()
    )

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val currentDateString: String =
            DateFormat.getDateInstance().format(calendar.time)
        birthDateTextView.text = currentDateString
    }

    private val uploadUserEventListener = object :
        UploadDataCallbacks {
        override fun onError(message: String) {
            showToast(this@RegistrationActivity, message)
        }

        override fun onSuccess() {
            showToast(this@RegistrationActivity, "Saved")
            finish()
        }

    }

    companion object {
        const val DEFAULT_IMAGE_URL =
            "https://www.pinclipart.com/picdir/big/164-1640714_user-symbol-interface-contact-phone-set-add-sign.png"

    }
}
