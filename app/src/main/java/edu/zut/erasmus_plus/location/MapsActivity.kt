package edu.zut.erasmus_plus.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.zut.erasmus_plus.location.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isRequestLoacation: Boolean = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            500)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult != null) {
                    super.onLocationResult(locationResult)
                    locationResult.lastLocation?.let {
                        val myPosition = it?.let {
                            LatLng(it.latitude,it.longitude)
                        }
                        myPosition?.let {
                            mMap.addMarker(MarkerOptions().position(myPosition).title("My position"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition))

                        }
                    }

                }
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    fun getLastPos(view: View)
    {
        checkPermission()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val myPosition = location?.let {
                    LatLng(it.latitude,it.longitude)
                }
                myPosition?.let {
                    mMap.addMarker(MarkerOptions().position(myPosition).title("My position"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition))
                }
            }

    }

    private fun checkPermission()
    {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
                isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    fun startStopRequestLocation(view: View)
    {
        checkPermission()
        if (!isRequestLoacation)
        {
            binding.btContinousPosition.text=getString(R.string.stop_loop)
            val addTask= fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            addTask.addOnCompleteListener {task->
                if (task.isSuccessful) {
                    Log.d("startStopRequestLocation", "Start loop Location Callback.")
                } else {
                    Log.d("startStopRequestLocation", "Failed start  Location Callback.")
                }
            }
        }
        else
        {
            binding.btContinousPosition.text=getString(R.string.start_loop)
            val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("startStopRequestLocation", "Location Callback removed.")
                } else {
                    Log.d("startStopRequestLocation", "Failed to remove Location Callback.")
                }
            }
        }
        isRequestLoacation=!isRequestLoacation
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}