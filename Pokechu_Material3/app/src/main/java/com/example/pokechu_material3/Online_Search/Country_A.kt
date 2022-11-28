package com.example.pokechu_material3.Online_Search

import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pokechu_material3.R
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class Country_A : AppCompatActivity() {
    var country_modelArrayList = ArrayList<Country_Model>()
    var recyclerView_event: RecyclerView? = null
    var sharedPreferences: SharedPreferences? = null
    var Searchtext: EditText? = null
    private var adapter: Country_Adapter? = null
    var img_add: ImageView? = null
    var str_url = "https://cdn.jsdelivr.net/npm/country-flag-emoji-json@2.0.0/dist/index.json"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country)
        (findViewById<View>(R.id.toolbr_lbl) as TextView).text = "Country"
        findViewById<View>(R.id.imgbck).setOnClickListener { onBackPressed() }
        country_modelArrayList = ArrayList()
        img_add = findViewById(R.id.img_add)
        recyclerView_event = findViewById(R.id.recyclerView_data)
        recyclerView_event?.let { it.setLayoutManager(
            LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
        )}
        bloodList
        /*Searchtext = findViewById<View>(R.id.search_input) as EditText
        Searchtext!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                filterQuery(editable.toString())
            }
        })*/
    }

    // params.put("user_id", str_userid);
    private val bloodList: Unit
        private get() {
            val progressDialog = ProgressDialog(this@Country_A)
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Loading..")
            progressDialog.show()
            val stringRequest: StringRequest =
                object : StringRequest(Method.GET, str_url, Response.Listener { response ->
                    Log.d("quick_1", "onResponse: $response")
                    try {
                        country_modelArrayList = ArrayList()
                        val jsonArrayvideo = JSONArray(response)
                        for (i in 0 until jsonArrayvideo.length()) {
                            val video = Country_Model()
                            val jsonObject1 = jsonArrayvideo.getJSONObject(i)
                            video.name = jsonObject1.getString("name")
                            video.code = jsonObject1.getString("code")
                            video.image = jsonObject1.getString("image")
                            country_modelArrayList.add(video)
                        }
                        adapter = Country_Adapter(country_modelArrayList, this@Country_A)
                        recyclerView_event!!.adapter = adapter
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        progressDialog.dismiss()
                    }
                    progressDialog.dismiss()
                }, Response.ErrorListener { error ->
                    error.printStackTrace()
                    progressDialog.dismiss()
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String>? {
                        val params = HashMap<String, String>()
                        // params.put("user_id", str_userid);
                        Log.d("parameter_tab", "getParams: $params")
                        return params
                    }

                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        return HashMap()
                    }
                }
            val requestQueue = Volley.newRequestQueue(applicationContext)
            val retryPolicy: RetryPolicy = DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            stringRequest.retryPolicy = retryPolicy
            requestQueue.add(stringRequest)
        }

    fun filterQuery(text: String?) {
        val filterdNames = ArrayList<Country_Model>()
        for (s in country_modelArrayList) {
            if (s.name.lowercase(Locale.getDefault())
                    .contains(text!!) || s.name.uppercase(Locale.getDefault()).contains(
                    text
                )
            ) {
                filterdNames.add(s)
            }
        }
        adapter!!.setFilter(filterdNames)
    }
}